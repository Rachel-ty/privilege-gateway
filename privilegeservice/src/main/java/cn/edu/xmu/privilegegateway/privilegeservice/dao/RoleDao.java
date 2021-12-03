package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
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
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private RolePrivilegePoMapper rolePrivilegePoMapper;

    @Autowired
    private PrivilegeDao privDao;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BaseCoder coder;//使用basecoder,便于日后进行加密（现在还没有加密）

    public final static String ROLEKEY = "r_%d";


    private final static String JudgePIDByKey= "cn/edu/xmu/privilegegateway/privilegeservice/lua/JudgePIDByKey.lua";
    public final static Byte ISBASEROLE=1;
    public final static Byte NOTBASEROLE=0;
    public final static Byte FORBIDEN=1;
    public final static Byte NORMAL=0;
    public final static Integer OK=0;
    public final static Integer MODIFIED=1;
    //功能角色查询权限key
    public final static String BASEROLEKEY="br_%d";
    public final static Collection<String> codeFields = new ArrayList<>();
    public final static List<String> signFields = new ArrayList<>(Arrays.asList("roleId", "privilegeId"));


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
     * 分页查询所有角色
     *
     * @author 24320182203281 王纬策
     * @param pageNum 页数
     * @param pageSize 每页大小
     * @return ReturnObject<List> 角色列表
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     */
    public ReturnObject<PageInfo<VoObject>> selectAllRole(Long departId, Integer pageNum, Integer pageSize) {
        RolePoExample example = new RolePoExample();
        RolePoExample.Criteria criteria = example.createCriteria();
        criteria.andDepartIdEqualTo(departId);
        //分页查询
        PageHelper.startPage(pageNum, pageSize);
        logger.debug("page = " + pageNum + "pageSize = " + pageSize);
        List<RolePo> rolePos = null;
        try {
            //不加限定条件查询所有
            rolePos = roleMapper.selectByExample(example);
            List<VoObject> ret = new ArrayList<>(rolePos.size());
            for (RolePo po : rolePos) {
                Role role = new Role(po);
                ret.add(role);
            }
            PageInfo<VoObject> rolePage = PageInfo.of(ret);
            return new ReturnObject<>(rolePage);
        }
        catch (DataAccessException e){
            logger.error("selectAllRole: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
    }
    public ReturnObject<PageInfo<VoObject>> selectAllBaseRole(Long did,Long roleid,Integer pageNum, Integer pageSize)
    {
        RolePoExample example = new RolePoExample();
        RolePoExample.Criteria criteria = example.createCriteria();
        List<RolePo> polist=new ArrayList<>();
        if(did==0)
        {
            polist.add(roleMapper.selectByPrimaryKey(roleid));
        }
        else
        {
            return null;
        }
        if(polist.size()==0)
        {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
       return null;
    }
    /**
     * 增加一个角色
     *
     * @author 24320182203281 王纬策
     * @param role 角色bo
     * @return ReturnObject<Role> 新增结果
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     */
    public ReturnObject<Role> insertRole(Role role) {
        RolePo rolePo = role.gotRolePo();
        ReturnObject<Role> retObj = null;
        try{
            int ret = roleMapper.insertSelective(rolePo);
            if (ret == 0) {
                //插入失败
                logger.debug("insertRole: insert role fail " + rolePo.toString());
                retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST, String.format("新增失败：" + rolePo.getName()));
            } else {
                //插入成功
                logger.debug("insertRole: insert role = " + rolePo.toString());
                role.setId(rolePo.getId());
                retObj = new ReturnObject<>(role);
            }
        }
        catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("auth_role.auth_role_name_uindex")) {
                //若有重复的角色名则新增失败
                logger.debug("updateRole: have same role name = " + rolePo.getName());
                retObj = new ReturnObject<>(ReturnNo.ROLE_EXIST, String.format("角色名重复：" + rolePo.getName()));
            } else {
                // 其他数据库错误
                logger.debug("other sql exception : " + e.getMessage());
                retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            }
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        return retObj;
    }

    /**
     * 删除一个角色
     *
     * @author 24320182203281 王纬策
     * @param id 角色id
     * @return ReturnObject<Object> 删除结果
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     */
    public ReturnObject<Object> deleteRole(Long did, Long id) {
        ReturnObject<Object> retObj = null;
        RolePoExample rolePoDid= new RolePoExample();
        RolePoExample.Criteria criteriaDid = rolePoDid.createCriteria();
        criteriaDid.andIdEqualTo(id);
        criteriaDid.andDepartIdEqualTo(did);
        try {
            int ret = roleMapper.deleteByExample(rolePoDid);
            if (ret == 0) {
                //删除角色表
                logger.debug("deleteRole: id not exist = " + id);
                retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST, String.format("角色id不存在：" + id));
            } else {
                //删除角色权限表
                logger.debug("deleteRole: delete role id = " + id);
                RolePrivilegePoExample exampleRP = new RolePrivilegePoExample();
                RolePrivilegePoExample.Criteria criteriaRP = exampleRP.createCriteria();
                criteriaRP.andRoleIdEqualTo(id);
                List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(exampleRP);
                logger.debug("deleteRole: delete role-privilege num = " + rolePrivilegePos.size());
                for (RolePrivilegePo rolePrivilegePo : rolePrivilegePos) {
                    rolePrivilegePoMapper.deleteByPrimaryKey(rolePrivilegePo.getId());
                }
                //删除缓存中角色权限信息
                redisTemplate.delete(String.format(ROLEKEY, id));
                //删除用户角色表
                UserRolePoExample exampleUR = new UserRolePoExample();
                UserRolePoExample.Criteria criteriaUR = exampleUR.createCriteria();
                criteriaUR.andRoleIdEqualTo(id);
                List<UserRolePo> userRolePos = userRolePoMapper.selectByExample(exampleUR);
                logger.debug("deleteRole: delete user-role num = " + userRolePos.size());
                for (UserRolePo userRolePo : userRolePos) {
                    userRolePoMapper.deleteByPrimaryKey(userRolePo.getId());
                    //删除缓存中具有删除角色的用户权限
                    redisTemplate.delete("u_" + userRolePo.getUserId());
                    redisTemplate.delete("up_" + userRolePo.getUserId());
                    //查询当前所有有效的代理具有删除角色用户的代理用户
                    UserProxyPoExample example = new UserProxyPoExample();
                    UserProxyPoExample.Criteria criteria = example.createCriteria();
                    criteria.andProxyUserIdEqualTo(userRolePo.getUserId());
                    List<UserProxyPo> userProxyPos = userProxyPoMapper.selectByExample(example);
                    for(UserProxyPo userProxyPo : userProxyPos){
                        //删除缓存中代理了具有删除角色的用户的代理用户
                        redisTemplate.delete("u_" + userProxyPo.getUserId());
                        redisTemplate.delete("up_" + userProxyPo.getUserId());
                    }
                }
                retObj = new ReturnObject<>();
            }

            return retObj;
        }
        catch (DataAccessException e){
            logger.error("selectAllRole: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
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
     */
    public ReturnObject<Role> updateRole(Role role) {
        RolePo rolePo = role.gotRolePo();
        ReturnObject<Role> retObj = null;
        RolePoExample rolePoExample = new RolePoExample();
        RolePoExample.Criteria criteria = rolePoExample.createCriteria();
        criteria.andIdEqualTo(role.getId());
        criteria.andDepartIdEqualTo(role.getDepartId());
        try{
            int ret = roleMapper.updateByExampleSelective(rolePo, rolePoExample);
//            int ret = roleMapper.updateByPrimaryKeySelective(rolePo);
            if (ret == 0) {
                //修改失败
                logger.debug("updateRole: update role fail : " + rolePo.toString());
                retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST, String.format("角色id不存在：" + rolePo.getId()));
            } else {
                //修改成功
                logger.debug("updateRole: update role = " + rolePo.toString());
                retObj = new ReturnObject<>();
            }
        }
        catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("auth_role.auth_role_name_uindex")) {
                //若有重复的角色名则修改失败
                logger.debug("updateRole: have same role name = " + rolePo.getName());
                retObj = new ReturnObject<>(ReturnNo.ROLE_EXIST, String.format("角色名重复：" + rolePo.getName()));
            } else {
                // 其他数据库错误
                logger.debug("other sql exception : " + e.getMessage());
                retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            }
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
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
     * @author: zhang yu
     * @date: 2021/11/25 20:04
     * @version: 1.0
     */
    /**
     * 查询功能角色权限，先根据roleid查询privilegeid,再根据privilegeidc查询
     *
     * @param rid
     * @param pagenum
     * @param pagesize
     * @return
     */
    public ReturnObject selectBaseRolePrivs(Long rid, Integer pagenum, Integer pagesize)
    {
        try{
            PageHelper.startPage(pagenum,pagesize);
            RolePo rolePo=roleMapper.selectByPrimaryKey(rid);
            //判断是否为功能角色
            if(rolePo==null||rolePo.getBaserole().equals(ISBASEROLE))
            {
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
            }
            RolePrivilegePoExample example=new RolePrivilegePoExample();
            RolePrivilegePoExample.Criteria criteria=example.createCriteria();
            criteria.andRoleIdEqualTo(rid);
            List<RolePrivilegePo> pos=rolePrivilegePoMapper.selectByExample(example);
            List<BaseRolePrivilegeRetVo> volist=new ArrayList<>();
            for(RolePrivilegePo po:pos)
            {
                RolePrivilegePo newpo=(RolePrivilegePo) coder.decode_check(po,null,codeFields,signFields,"signature");
                //查询对应的权限信息
                Privilege privilege=privDao.findPriv(po.getPrivilegeId());
                BaseRolePrivilegeRetVo vo=(BaseRolePrivilegeRetVo)Common.cloneVo(newpo,BaseRolePrivilegeRetVo.class);
                Integer sign=OK;
                if(privilege.getSignature()==null)
                {
                   sign=MODIFIED;
                }
                if(newpo.getSignature()==null)
                {
                    sign=MODIFIED;
                }
                vo.setSign(sign);
                vo.setName(privilege.getName());
                vo.setId(privilege.getId());
                volist.add(vo);
            }
            PageInfo<BaseRolePrivilegeRetVo> pageInfo=new PageInfo<>(volist);
            ReturnObject returnObject=new ReturnObject(pageInfo);
            return Common.getPageRetVo(returnObject,BaseRolePrivilegeRetVo.class);
        }catch(Exception e)
        {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,String.format("数据库发生错误",e.getMessage()));
        }
    }
    /**
     * 由Role Id, Privilege Id 增加功能角色权限
     * created by 王琛 24320182203277
     * modified by zhangyu
     */
    /**
     * @return
     */
    /**
     *
     * @param roleid
     * @param privilegeid
     * @param creatorid
     * @param creatorname
     * @return
     */
    public ReturnObject addBaseRolePriv(Long roleid,Long privilegeid,Long creatorid,String creatorname){
        try
        {
            RolePrivilegePo rolePrivilegePo=new RolePrivilegePo();
            rolePrivilegePo.setRoleId(roleid);
            rolePrivilegePo.setPrivilegeId(privilegeid);
            Common.setPoModifiedFields(rolePrivilegePo,creatorid,creatorname);
            Privilege privilege=privDao.findPriv(privilegeid);
            if(privilege==null)
                return new ReturnObject(ReturnNo.PRIVILEGE_RELATION_EXIST);
            RolePrivilegePo newpo=(RolePrivilegePo)coder.code_sign(rolePrivilegePo,RolePrivilegePo.class,codeFields,signFields,"signature");
            rolePrivilegePoMapper.insertSelective(newpo);
            RolePrivilegePo retpo=rolePrivilegePoMapper.selectByPrimaryKey(newpo.getId());
            RolePrivilegePo newretpo=(RolePrivilegePo) coder.decode_check(retpo,RolePrivilegePo.class,codeFields,signFields,"signature");
            BaseRolePrivilegeRetVo vo=(BaseRolePrivilegeRetVo) Common.cloneVo(newretpo,BaseRolePrivilegeRetVo.class);
            Integer sign=0;
            if(newretpo.getSignature()==null)
            {
                sign=MODIFIED;
            }
            if(privilege.getSign().equals(MODIFIED))
            {
                sign=MODIFIED;
            }
            vo.setSign(sign);
            vo.setName(privilege.getName());
            vo.setId(privilege.getId());
            return new ReturnObject(vo);

        }catch (DuplicateFormatFlagsException e)
        {
            return  new ReturnObject(ReturnNo.URL_SAME);
        }
        catch (Exception e)
        {
            return new ReturnObject(ReturnNo.PRIVILEGE_RELATION_EXIST);
        }

    }

    /**
     * @author: zhang yu
     * @date: 2021/11/25 20:11
     * @version: 1.0
     */
    /**
     * 由角色id,privilegeid删除角色对应权限
     * @param rid
     * @param pid
     * @return
     */

    public ReturnObject delBaseRolePriv(Long rid,Long pid){
        try {
            RolePrivilegePoExample example = new RolePrivilegePoExample();
            RolePrivilegePoExample.Criteria criteria = example.createCriteria();
            criteria.andRoleIdEqualTo(rid);
            criteria.andPrivilegeIdEqualTo(pid);
            String key=String.format(BASEROLEKEY,rid);
            redisUtil.del(key);
            int ret = rolePrivilegePoMapper.deleteByExample(example);
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }
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
