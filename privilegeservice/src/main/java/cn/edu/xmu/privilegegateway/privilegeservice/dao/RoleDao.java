package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.*;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.RolePrivilege;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.*;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.encript.SHA256;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 角色访问类
 * @author Ming Qiu
 * createdBy Ming Qiu 2020/11/02 13:57
 * modifiedBy 王纬策 2020/11/7 19:20
 **/
@Repository
public class RoleDao {

    private static final Logger logger = LoggerFactory.getLogger(RoleDao.class);

    @Value("${privilegeservice.role.expiretime}")
    private long timeout;

    @Autowired
    private RolePoMapper roleMapper;

    @Autowired
    private UserPoMapper userMapper;

    @Autowired
    private PrivilegePoMapper privilegePoMapper;

    @Autowired
    private UserRolePoMapper userRolePoMapper;

    @Autowired
    private UserProxyPoMapper userProxyPoMapper;

    @Autowired
    private RoleInheritedPoMapper roleInheritedPoMapper;

    @Autowired
    private RolePrivilegePoMapper rolePrivilegePoMapper;

    @Autowired
    private PrivilegeDao privDao;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BaseCoder baseCoder;

    private BaseSign baseSign = new BaseSign() {
        @Override
        protected String encrypt(String content) {
            return null;
        }
    };

    public final static String ROLEKEY = "r_%d";

    final static Collection<String> codeFields = new ArrayList<>();
    final static List<String> roleInheritedSignFields = new ArrayList<>(Arrays.asList("roleId", "roleCId"));
    final static List<String> userRoleSignFields = new ArrayList<>(Arrays.asList("userId", "roleId"));
    final static Collection<String> userRoleCodeFields = new ArrayList<>();

    /**
     * 根据角色Id,查询角色的所有权限
     * @author yue hao
     * @param id 角色ID
     * @return 角色的权限列表
     */
    public List<Privilege> findPrivsByRoleId(Long id) {
        //getPrivIdsByRoleId已经进行role的签名校验
        List<Long> privIds = this.getPrivIdsByRoleId(id);
        List<Privilege> privileges = new ArrayList<>();
        for(Long privId: privIds) {
            Privilege po = this.privDao.findPriv(privId);
            logger.debug("findPrivsByRoleId:  po = " + po);
            privileges.add(po);
        }
        return privileges;
    }

    /**
     * 将一个角色的所有权限id载入到Redis
     *
     * @param id 角色id
     * @return void
     *
     * createdBy: Ming Qiu 2020-11-02 11:44
     * ModifiedBy: Ming Qiu 2020-11-03 12:24
     * 将读取权限id的代码独立为getPrivIdsByRoleId. 增加redis值的有效期
     *            Ming Qiu 2020-11-07 8:00
     * 集合里强制加“0”
     */
    public void loadRolePriv(Long id) {
        List<Long> privIds = this.getPrivIdsByRoleId(id);
        String key = String.format(ROLEKEY, id);
        for (Long pId : privIds) {
            redisUtil.addSet(key, pId);
        }
        redisUtil.addSet(key,0);
        long randTimeout = Common.addRandomTime(this.timeout);
        redisUtil.expire(key, randTimeout, TimeUnit.SECONDS);
    }

    /**
     * 由Role Id 获得 Privilege Id 列表
     *
     * @param id: Role id
     * @return Privilege Id 列表
     * created by Ming Qiu in 2020/11/3 11:48
     */
    private List<Long> getPrivIdsByRoleId(Long id) {
        RolePrivilegePoExample example = new RolePrivilegePoExample();
        RolePrivilegePoExample.Criteria criteria = example.createCriteria();
        criteria.andRoleIdEqualTo(id);
        List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(example);
        List<Long> retIds = new ArrayList<>(rolePrivilegePos.size());
        for (RolePrivilegePo po : rolePrivilegePos) {
            StringBuilder signature = Common.concatString("-", po.getRoleId().toString(),
                    po.getPrivilegeId().toString());
            String newSignature = SHA256.getSHA256(signature.toString());

            if (newSignature.equals(po.getSignature())) {
                retIds.add(po.getPrivilegeId());
                logger.debug("getPrivIdsBByRoleId: roleId = " + po.getRoleId() + " privId = " + po.getPrivilegeId());
            } else {
                logger.info("getPrivIdsBByRoleId: Wrong Signature(auth_role_privilege): id =" + po.getId());
            }
        }
        return retIds;
    }

    public void initialize() throws Exception {
        RolePrivilegePoExample example = new RolePrivilegePoExample();
        RolePrivilegePoExample.Criteria criteria = example.createCriteria();
        criteria.andSignatureIsNull();
        List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(example);
        List<Long> retIds = new ArrayList<>(rolePrivilegePos.size());
        for (RolePrivilegePo po : rolePrivilegePos) {
            StringBuilder signature = Common.concatString("-", po.getRoleId().toString(),
                    po.getPrivilegeId().toString());
            String newSignature = SHA256.getSHA256(signature.toString());
            RolePrivilegePo newPo = new RolePrivilegePo();
            newPo.setId(po.getId());
            newPo.setSignature(newSignature);
            rolePrivilegePoMapper.updateByPrimaryKeySelective(newPo);
        }

    }

    /**
     * 部门下是否已存在用户名
     *
     * @author 王文凯
     */
    public boolean roleExist(Long did, String name) {
        RolePoExample example = new RolePoExample();
        RolePoExample.Criteria criteria = example.createCriteria();
        criteria.andNameEqualTo(name);
        criteria.andDepartIdEqualTo(did);

        List<RolePo> list = roleMapper.selectByExample(example);
        return list.size() != 0;
    }

    /**
     * 分页查询所有角色
     *
     * @author 24320182203281 王纬策
     * @param page 页数
     * @param pageSize 每页大小
     * @return ReturnObject<List> 角色列表
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 10:51
     */
    public ReturnObject selectAllRole(Long did, Integer page, Integer pageSize) {
        RolePoExample example = new RolePoExample();
        RolePoExample.Criteria criteria = example.createCriteria();
        criteria.andDepartIdEqualTo(did);
        // 分页查询
        logger.debug("page = " + page + "pageSize = " + pageSize);
        PageHelper.startPage(page, pageSize);
        try {
            List<RolePo> pos = roleMapper.selectByExample(example);

            PageInfo info = PageInfo.of(pos);
            ReturnObject retObj = Common.getPageRetVo(new ReturnObject<>(info), RoleRetVo.class);

            return new ReturnObject<>(retObj);
        } catch (Exception e) {
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
    }

    /**
     * 增加一个角色
     *
     * @author 24320182203281 王纬策
     * @param role 角色bo
     * @return ReturnObject<Role> 新增结果
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:04
     */
    public ReturnObject insertRole(Role role) {
        RolePo rolePo = (RolePo) Common.cloneVo(role, RolePo.class);
        try {
            if (roleExist(role.getDepartId(), role.getName())) {
                return new ReturnObject(ReturnNo.ROLE_EXIST);
            }

            int ret = roleMapper.insertSelective(rolePo);
            if (ret == 0) {
                // 插入失败
                logger.debug("insertRole: insert role fail " + rolePo.toString());
                return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST, String.format("新增失败：" + rolePo.getName()));
            } else {
                // 插入成功
                logger.debug("insertRole: insert role = " + rolePo.toString());
                role.setId(rolePo.getId());
                return new ReturnObject<>(role);
            }
        }  catch (Exception e) {
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 删除一个角色
     *
     * @author 24320182203281 王纬策
     * @param roleId 角色id
     * @return ReturnObject<Object> 删除结果
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:16
     */
    public ReturnObject deleteRole(Long roleId, Long did) {
        try {
            if (!checkRoleDid(roleId, did)) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE, "部门id不匹配");
            }

            int ret = roleMapper.deleteByPrimaryKey(roleId);
            if (ret == 0) {
                // 删除角色表
                logger.debug("deleteRole: roleId not exist = " + roleId);
                return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST, String.format("角色id不存在：" + roleId));
            } else {
                // 删除角色权限表
                logger.debug("deleteRole: delete role roleId = " + roleId);
                RolePrivilegePoExample exampleRP = new RolePrivilegePoExample();
                RolePrivilegePoExample.Criteria criteriaRP = exampleRP.createCriteria();
                criteriaRP.andRoleIdEqualTo(roleId);
                List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(exampleRP);
                logger.debug("deleteRole: delete role-privilege num = " + rolePrivilegePos.size());
                for (RolePrivilegePo rolePrivilegePo : rolePrivilegePos) {
                    rolePrivilegePoMapper.deleteByPrimaryKey(rolePrivilegePo.getId());
                }
                // 删除缓存中角色权限信息
                redisUtil.del(String.format(ROLEKEY, roleId));
                // 删除用户角色表
                UserRolePoExample exampleUR = new UserRolePoExample();
                UserRolePoExample.Criteria criteriaUR = exampleUR.createCriteria();
                criteriaUR.andRoleIdEqualTo(roleId);
                List<UserRolePo> userRolePos = userRolePoMapper.selectByExample(exampleUR);
                logger.debug("deleteRole: delete user-role num = " + userRolePos.size());
                for (UserRolePo userRolePo : userRolePos) {
                    userRolePoMapper.deleteByPrimaryKey(userRolePo.getId());
                    // 删除缓存中具有删除角色的用户权限
                    redisUtil.del("u_" + userRolePo.getUserId());
                    redisUtil.del("up_" + userRolePo.getUserId());
                    // 查询当前所有有效的代理具有删除角色用户的代理用户
                    UserProxyPoExample example = new UserProxyPoExample();
                    UserProxyPoExample.Criteria criteria = example.createCriteria();
                    criteria.andProxyUserIdEqualTo(userRolePo.getUserId());
                    List<UserProxyPo> userProxyPos = userProxyPoMapper.selectByExample(example);
                    for (UserProxyPo userProxyPo : userProxyPos) {
                        // 删除缓存中代理了具有删除角色的用户的代理用户
                        redisUtil.del("u_" + userProxyPo.getUserId());
                        redisUtil.del("up_" + userProxyPo.getUserId());
                    }
                }
                return new ReturnObject<>();
            }
        } catch (Exception e) {
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
    }

    /**
     * 修改一个角色
     *
     * @author 24320182203281 王纬策
     * @param role 角色bo
     * @return ReturnObject<Role> 修改结果
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:24
     */
    public ReturnObject updateRole(Role role) {
        RolePo rolePo = (RolePo) Common.cloneVo(role, RolePo.class);
        try {
            if (roleExist(role.getDepartId(), role.getName())) {
                return new ReturnObject(ReturnNo.ROLE_EXIST);
            }

            if (!checkRoleDid(role.getId(), role.getDepartId())) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE, "部门id不匹配");
            }

            int ret = roleMapper.updateByPrimaryKeySelective(rolePo);
            if (ret == 0) {
                // 修改失败
                logger.debug("updateRole: update role fail : " + rolePo.toString());
                return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST, String.format("角色id不存在：" + rolePo.getId()));
            } else {
                // 修改成功
                logger.debug("updateRole: update role = " + rolePo.toString());
                return new ReturnObject<>();
            }
        } catch (Exception e) {
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
    }

    /**
     * 查询角色中用户
     *
     * @author 22920192204289 王文凯
     */
    public ReturnObject selectUserByRole(Long roleId, Long did, Integer page, Integer pageSize) {
        try {
            if (!checkRoleDid(roleId, did)) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE, String.format("部门id不匹配"));
            }

            UserRolePoExample example = new UserRolePoExample();
            UserRolePoExample.Criteria criteria = example.createCriteria();
            criteria.andRoleIdEqualTo(roleId);
            // 分页查询
            PageHelper.startPage(page, pageSize);
            logger.debug("page = " + page + "pageSize = " + pageSize);
            // 查询角色所有用户
            List<UserRolePo> userRolePos = userRolePoMapper.selectByExample(example);
            List<User> users = new ArrayList<>(userRolePos.size());
            for (UserRolePo po : userRolePos) {
                UserRole userRole = (UserRole) baseCoder.decode_check(po, UserRole.class, codeFields, userRoleSignFields, "signature");
                User user = (User) Common.cloneVo(userMapper.selectByPrimaryKey(po.getUserId()), User.class);
                if (userRole.getSignature() != null) {
                    user.setSign((byte)0);
                } else {
                    user.setSign((byte)1);
                }
                users.add(user);
            }

            PageInfo info = PageInfo.of(users);
            info.setPageNum(page);
            info.setPageSize(pageSize);
            return Common.getPageRetVo(new ReturnObject<>(info), RoleRetVo.class);
        } catch (Exception e) {
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
    }

    /**
     * 由Role Id, Privilege Id 增加 角色权限
     *
     * @param  roleid, Privilegeid, userid
     * @return RolePrivilegeRetVo
     * created by 王琛 24320182203277
     */
    public ReturnObject<VoObject> addPrivByRoleIdAndPrivId(Long roleid, Long privid, Long userid){
        UserPo userpo = userMapper.selectByPrimaryKey(userid);
        PrivilegePo privilegepo = privilegePoMapper.selectByPrimaryKey(privid);
        RolePo rolePo = roleMapper.selectByPrimaryKey(roleid);
        if(userpo==null || privilegepo==null || rolePo==null){
            return new ReturnObject<VoObject>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        ReturnObject<Role> retObj = null;
        //获取当前时间
        LocalDateTime localDateTime = LocalDateTime.now();
        RolePrivilege rolePrivilege = new RolePrivilege();

        //查询是否角色已经存在此权限
        RolePrivilegePoExample example = new RolePrivilegePoExample();
        RolePrivilegePoExample.Criteria criteria = example.createCriteria();
        criteria.andPrivilegeIdEqualTo(privid);
        criteria.andRoleIdEqualTo(roleid);
        List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(example);
        RolePrivilegePo roleprivilegepo = new RolePrivilegePo();

        if(rolePrivilegePos.isEmpty()){
            roleprivilegepo.setRoleId(roleid);
            roleprivilegepo.setPrivilegeId(privid);
            roleprivilegepo.setCreatorId(userid);
            roleprivilegepo.setGmtCreate(localDateTime);

            StringBuilder signature = Common.concatString("-", roleprivilegepo.getRoleId().toString(),
                    roleprivilegepo.getPrivilegeId().toString(), roleprivilegepo.getCreatorId().toString(), localDateTime.toString());
            String newSignature = SHA256.getSHA256(signature.toString());
            roleprivilegepo.setSignature(newSignature);

            try {
                int ret = rolePrivilegePoMapper.insert(roleprivilegepo);

                if (ret == 0) {
                    //插入失败
                    return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
                } else {
                    //插入成功
                    //清除角色权限
                    String key = String.format(ROLEKEY, roleid);
                    if(redisTemplate.hasKey(key)){
                        redisTemplate.delete(key);
                    }
                }
            }catch (DataAccessException e){
                // 数据库错误
                return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            }catch (Exception e) {
                // 其他Exception错误
                return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了错误：%s", e.getMessage()));
            }

        }else{
//            FIELD_NOTVALID
            return new ReturnObject<>(ReturnNo.FIELD_NOTVALID, String.format("角色权限已存在"));
        }

        //组装返回的bo
        rolePrivilege.setId(roleprivilegepo.getId());
        rolePrivilege.setCreator(userpo);
        rolePrivilege.setRole(rolePo);
        rolePrivilege.setPrivilege(privilegepo);
        rolePrivilege.setGmtModified(localDateTime.toString());

        return new ReturnObject<VoObject>(rolePrivilege);
    }

    /**
     * 由RolePrivilege Id 删除 角色权限
     *
     * @param id: RolePrivilege Id
     * @return void
     * created by 王琛 24320182203277
     */

    public ReturnObject<Object> delPrivByPrivRoleId(Long id){
        ReturnObject<Object> retObj = null;
        RolePrivilegePo rolePrivilegePo = rolePrivilegePoMapper.selectByPrimaryKey(id);
        int ret = rolePrivilegePoMapper.deleteByPrimaryKey(id);
        if(ret==0){
            retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }else{
            Long roleid = rolePrivilegePo.getRoleId();
            String key = String.format(ROLEKEY, roleid);
            //清除缓存被删除的的角色,重新load
            if(redisTemplate.hasKey(key)){
                redisTemplate.delete(key);
            }
            retObj = new ReturnObject<>();
        }

        return retObj;
    }

    /**
     * 由Role Id 获取 角色权限
     *
     * @param id: Role Id
     * @return List<RolePrivilegeRetVo>
     * created by 王琛 24320182203277
     */

    public ReturnObject<List> getRolePrivByRoleId(Long id){
        String key = String.format(ROLEKEY, id);
        List<RolePrivilege> rolepribilegere = new ArrayList<>();
        RolePrivilegePoExample example = new RolePrivilegePoExample();
        RolePrivilegePoExample.Criteria criteria = example.createCriteria();

        //查看是否有此角色
        RolePo rolePo = roleMapper.selectByPrimaryKey(id);
        if(rolePo==null){
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        RolePrivilege e = new RolePrivilege();

        List<Long> privids = getPrivIdsByRoleId(id);

        for(Long pid: privids){
            example.clear();
            criteria.andRoleIdEqualTo(id);
            criteria.andPrivilegeIdEqualTo(pid);
            List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(example);
            if(rolePrivilegePos!=null && rolePrivilegePos.size()>0 && rolePrivilegePos.get(0)!=null){

                UserPo userpo = userMapper.selectByPrimaryKey(rolePrivilegePos.get(0).getCreatorId());
                PrivilegePo privilegepo = privilegePoMapper.selectByPrimaryKey(pid);

                //组装权限bo
                e.setCreator(userpo);
                e.setId(pid);
                e.setPrivilege(privilegepo);
                e.setRole(rolePo);
                e.setGmtModified(rolePrivilegePos.get(0).getGmtCreate().toString());

                rolepribilegere.add(e);
            }
        }

        return new ReturnObject<>(rolepribilegere);
    }

    /**
     * 获得用户的角色id
     *
     * @param id 用户id
     * @return 角色id列表
     * createdBy: Ming Qiu 2020/11/3 13:55
     */
    public List<Long> getRoleIdByUserId(Long id) {
        UserRolePoExample example = new UserRolePoExample();
        UserRolePoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(id);
        List<UserRolePo> userRolePoList = userRolePoMapper.selectByExample(example);
        logger.debug("getRoleIdByUserId: userId = " + id + "roleNum = " + userRolePoList.size());
        List<Long> retIds = new ArrayList<>(userRolePoList.size());
        for (UserRolePo po : userRolePoList) {
            StringBuilder signature = Common.concatString("-",
                    po.getUserId().toString(), po.getRoleId().toString());
            String newSignature = SHA256.getSHA256(signature.toString());

            if (newSignature.equals(po.getSignature())) {
                retIds.add(po.getRoleId());
                logger.debug("getRoleIdByUserId: userId = " + po.getUserId() + " roleId = " + po.getRoleId());
            } else {
                logger.error("getRoleIdByUserId: 签名错误(auth_user_role): id =" + po.getId());
            }
        }
        return retIds;
    }

    /**
     * @description 检查角色的departid是否与路径上的一致
     * @param roleid 角色id
     * @param departid 路径上的departid
     * @return boolean
     * @author Xianwei Wang
     * created at 11/20/20 1:51 PM
     */
    public boolean checkRoleDid(Long roleid, Long departid) {
        RolePo rolePo = roleMapper.selectByPrimaryKey(roleid);
        if (rolePo == null) {
            return false;
        }
        if (!rolePo.getDepartId().equals(departid)) {
            return false;
        }
        return true;
    }

    /**
     * 设置角色继承关系
     * @return ReturnObject
     * @author 张晖婧
     * */
    public ReturnObject createRoleInherited(RoleInherited roleInherited, Long did) {
        RolePo prolePo = roleMapper.selectByPrimaryKey(roleInherited.getRoleId());
        RolePo crolePo = roleMapper.selectByPrimaryKey(roleInherited.getRoleCId());
        UserPo createPo = userMapper.selectByPrimaryKey(roleInherited.getCreatorId());

        //用户id或角色id不存在
        if (prolePo == null || createPo == null || crolePo == null) {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        if (checkRoleDid(roleInherited.getRoleCId(), did) && (checkRoleDid(roleInherited.getRoleId(), did) || prolePo.getBaserole().equals((byte)0))) {

            ReturnObject retObj = null;

            RoleInheritedPo roleInheritedPo =(RoleInheritedPo) baseCoder.code_sign(roleInherited, RoleInheritedPo.class, codeFields, roleInheritedSignFields, "signature");

            try {
                roleInheritedPoMapper.insertSelective(roleInheritedPo);

                RoleInherited roleInheritedBo=(RoleInherited) Common.cloneVo(roleInheritedPo,RoleInherited.class);
                roleInheritedBo.setSign((byte)0);
                //组装子角色的id和名称
                roleInheritedBo.setId(crolePo.getId());
                roleInheritedBo.setName(crolePo.getName());

                return new ReturnObject<>(roleInheritedBo);

            } catch (DataAccessException e) {
                if (Objects.requireNonNull(e.getMessage()).contains("Duplicate entry")) {
                    return new ReturnObject<>(ReturnNo.ROLE_RELATION_EXIST,"重复继承角色");
                }
                retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            } catch (Exception e) {
                retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了内部错误：%s", e.getMessage()));
            }
            return new ReturnObject<>();
        }
        else {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE);
        }
    }

    /**
     * auth014: 查询角色的功能角色
     * @param did: 部门 id
     * @param id: 用户 id
     * @return Object
     * @author 22920192204320 张晖婧
     */
    public ReturnObject findBaserolesByRoleId(Long did, Long id, Integer page, Integer pageSize) {
        if (!checkRoleDid(id, did))
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE, "操作的角色id不在该部门");
        RoleInheritedPoExample example = new RoleInheritedPoExample();
        RoleInheritedPoExample.Criteria criteria = example.createCriteria();
        criteria.andRoleCIdEqualTo(id);

        logger.debug("page = " + page + "pageSize = " + pageSize);
        try {
            // 查询角色所有父角色
            List<RoleInheritedPo> roleInheritedPos = roleInheritedPoMapper.selectByExample(example);
            if (roleInheritedPos.size() == 0) {
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("page", 0);
                data.put("pageSize", pageSize);
                data.put("total", 0);
                data.put("pages", 0);
                data.put("list", null);
                return new ReturnObject(data);
            }

            Map<Long, Byte> roleIdMap = new HashMap<>((int) (roleInheritedPos.size() / 0.75) + 1);
            for (RoleInheritedPo po : roleInheritedPos) {
                RoleInherited roleInherited = (RoleInherited) baseCoder.decode_check(po, RoleInherited.class, codeFields, roleInheritedSignFields, "signature");
                if (roleInherited.getSignature() != null) {
                    roleIdMap.put(po.getRoleId(), (byte) 0);
                } else {
                    roleIdMap.put(po.getRoleId(), (byte) 1);
                }
            }

            // 筛选baserole=0
            RolePoExample rolePoExample = new RolePoExample();
            for (Long roleId : roleIdMap.keySet()) {
                RolePoExample.Criteria c = rolePoExample.createCriteria();
                c.andIdEqualTo(roleId);
                c.andBaseroleEqualTo((byte) 0);
                rolePoExample.or(c);
            }
            // 分页查询
            PageHelper.startPage(page, pageSize);
            List<RolePo> rolePos = roleMapper.selectByExample(rolePoExample);

            List<Role> roles = new ArrayList<>(rolePos.size());
            for (RolePo po : rolePos) {
                Role bo = (Role) Common.cloneVo(po, Role.class);
                bo.setSign(roleIdMap.get(po.getId()));
                roles.add(bo);
            }

            PageInfo info = PageInfo.of(roles);
            info.setPageNum(page);
            info.setPageSize(pageSize);
            return Common.getPageRetVo(new ReturnObject<>(info), RoleRetVo.class);
        } catch (DataAccessException e) {
            logger.error("selectAllRole: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }
    /**
     * 查询父角色
     * @author 张晖婧 22920192204320
     * @param did: 部门 id
     * @param id: 角色 id
     * @return Object
     */
    public ReturnObject findParentRoles(Long did, Long id, Integer page, Integer pageSize) {
        if (!checkRoleDid(id, did))
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE, "操作的角色id不在该部门");
        RoleInheritedPoExample example = new RoleInheritedPoExample();
        RoleInheritedPoExample.Criteria criteria = example.createCriteria();
        criteria.andRoleCIdEqualTo(id);

        // 查询角色所有父角色id
        List<RoleInheritedPo> roleInheritedPos = roleInheritedPoMapper.selectByExample(example);
        if (roleInheritedPos.size() == 0) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("page", 0);
            data.put("pageSize", pageSize);
            data.put("total", 0);
            data.put("pages", 0);
            data.put("list", null);
            return new ReturnObject(data);
        }

        List<RoleRetVo> roleRetVos = new ArrayList<>(roleInheritedPos.size());
        for (RoleInheritedPo po : roleInheritedPos) {
            RoleInherited roleInherited = (RoleInherited) baseCoder.decode_check(po, RoleInherited.class, codeFields, roleInheritedSignFields, "signature");
            RolePo rolePo = roleMapper.selectByPrimaryKey(roleInherited.getRoleId());

            RoleRetVo roleRetVo = (RoleRetVo) Common.cloneVo(rolePo, RoleRetVo.class);
            if (roleInherited.getSignature() != null) {
                roleRetVo.setSign((byte) 0);
            } else {
                roleRetVo.setSign((byte) 1);
            }

            roleRetVos.add(roleRetVo);
        }

        PageInfo info = PageInfo.of(roleRetVos);
        info.setPageNum(page);
        info.setPageSize(pageSize);
        return Common.getPageRetVo(new ReturnObject<>(info), RoleRetVo.class);
    }

    /**
     * 角色的影响力分析
     * 任务3-6
     * 删除和禁用，修改角色的继承关系时，删除所有影响的rediskey
     *
     * @param roleId 角色id
     * @return 影响的role，group和user的redisKey
     */
    public List<String> roleImpact(Long roleId){
        return null;
    }
}
