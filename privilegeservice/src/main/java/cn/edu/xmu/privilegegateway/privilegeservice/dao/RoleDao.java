/**
 * Copyright School of Informatics Xiamen University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.*;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.RolePrivilege;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.*;
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

    @Autowired@Lazy
    private GroupDao groupDao;

    @Autowired @Lazy
    private UserDao userDao;

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
    private GroupRolePoMapper groupRolePoMapper;

    @Autowired
    private RoleInheritedPoMapper roleInheritedPoMapper;

    @Autowired
    PrivilegeDao privDao;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private GroupRoleDao groupRoleDao;
    @Autowired
    private UserRoleDao userRoleDao;
    /**
     * 用户的redis key：r_id values:set{br_id};
     */
    public final static String ROLEKEY = "r_%d";
    public final static String BASEROLEKEY = "br_%d";

    /**
     * 功能用户的redis key:br_id values:set{privId};
     */
    private final static int BANED = 2;

    private final static int BASEROLE = 1;
    @Autowired
    private BaseCoder baseCoder;

    final static List<String> newUserRoleSignFields = new ArrayList<>(Arrays.asList("userId", "roleId"));

    final static List<String> newGroupRoleSignFields = new ArrayList<>(Arrays.asList("roleId", "groupId"));

    final static List<String> newRoleInheritedSignFields = new ArrayList<>(Arrays.asList("roleId", "roleCId"));

    /**
     * 根据角色Id,查询角色的所有权限
     * @author yue hao
     * @param id 角色ID
     * @return 角色的权限列表
     * modifiedBy Ming Qiu 2021/12/4 10:08
     */
    public List<Privilege> findPrivsByRoleId(Long id) {
        //getPrivIdsByRoleId已经进行role的签名校验
        ReturnObject returnObject = privDao.getPrivIdsByRoleId(id);
        if(returnObject.getCode()!=ReturnNo.OK){
            return null;
        }
        List<Long> privIds = (List<Long>) returnObject.getData();
        List<Privilege> privileges = new ArrayList<>();
        for(Long privId: privIds) {
            Privilege po = this.privDao.findPriv(privId);
            logger.debug("findPrivsByRoleId:  po = " + po);
            privileges.add(po);
        }
        return privileges;
    }

    /**
     * 根据roleId获得继承的roleIds
     * @param roleId
     * @return
     * CreateBy RenJieZheng 22920192204334
     */
    public ReturnObject getParentRole(Long roleId){
        try{
            RoleInheritedPoExample example = new RoleInheritedPoExample();
            RoleInheritedPoExample.Criteria criteria = example.createCriteria();
            criteria.andRoleCIdEqualTo(roleId);
            List<RoleInheritedPo>roleInheritedPos = roleInheritedPoMapper.selectByExample(example);
            logger.debug("getSuperiorRoleIdsByRoleId: roleId = " + roleId);
            List<GroupRelationPo>ret = (List<GroupRelationPo>) Common.listDecode(roleInheritedPos,RoleInheritedPo.class,baseCoder,null,newRoleInheritedSignFields,"signature",false);
            return new ReturnObject(ret);
        }catch (Exception e){
            logger.error("getSuperiorRoleIdsByRoleId: "+e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }

    /**
     * 将一个角色的功能角色id载入到Redis
     * @param id roleId
     * @return
     * CreateBy RenJieZheng 22920192204334
     */
    public ReturnObject loadRole(Long id){
        try{
            String key = String.format(ROLEKEY, id);
            RolePo rolePo = roleMapper.selectByPrimaryKey(id);

            //用户被禁止,则退出
            if(rolePo!=null&&rolePo.getState()!=null&&rolePo.getState()==BANED){
                //因为有redisUtil.unionAndStoreSet(roleKeys,key);所以被禁止也赋予0
                redisUtil.addSet(String.format(ROLEKEY,id),0);
                return new ReturnObject(ReturnNo.OK);
            }


            //如果是功能角色则加入
            if(rolePo.getBaserole()==BASEROLE){
                String brKey = String.format(BASEROLEKEY,id);
                redisUtil.addSet(String.format(ROLEKEY,id),brKey);
                return new ReturnObject(ReturnNo.OK);
            }

            //如果不是功能角色则继续往上找
            ReturnObject returnObject1 = getParentRole(id);
            if(returnObject1.getCode()!= ReturnNo.OK){
                return returnObject1;
            }
            List<RoleInheritedPo>roleInheritedPos = (List<RoleInheritedPo>) returnObject1.getData();
            //没有获得继承角色则是根角色，退出
            if(roleInheritedPos.size()<=0){
                redisUtil.addSet(String.format(ROLEKEY,id),0);
                return new ReturnObject<>(ReturnNo.OK);
            }
            List<String> roleKeys = new ArrayList<>();
            for(RoleInheritedPo roleInheritedPo:roleInheritedPos){
                if(!redisUtil.hasKey(String.format(ROLEKEY,roleInheritedPo.getRoleId()))){
                    ReturnObject returnObject2 = loadRole(roleInheritedPo.getRoleId());
                    if(returnObject2.getCode()!=ReturnNo.OK){
                        return returnObject2;
                    }
                }
                roleKeys.add(String.format(ROLEKEY,roleInheritedPo.getRoleId()));
            }
            if(roleKeys.size()>0){
                redisUtil.unionAndStoreSet(key,roleKeys,key);
            }
            redisUtil.addSet(key,0);
            long randTimeout = Common.addRandomTime(timeout);
            redisUtil.expire(key, randTimeout, TimeUnit.SECONDS);
            return new ReturnObject<>(ReturnNo.OK);
        }catch(Exception e){
            logger.error("loadRole: "+e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
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
//        RolePoExample rolePoDid= new RolePoExample();
//        RolePoExample.Criteria criteriaDid = rolePoDid.createCriteria();
//        criteriaDid.andIdEqualTo(id);
//        criteriaDid.andDepartIdEqualTo(did);
//        try {
//            int ret = roleMapper.deleteByExample(rolePoDid);
//            if (ret == 0) {
//                //删除角色表
//                logger.debug("deleteRole: id not exist = " + id);
//                retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST, String.format("角色id不存在：" + id));
//            } else {
//                //删除角色权限表
//                logger.debug("deleteRole: delete role id = " + id);
//                RolePrivilegePoExample exampleRP = new RolePrivilegePoExample();
//                RolePrivilegePoExample.Criteria criteriaRP = exampleRP.createCriteria();
//                criteriaRP.andRoleIdEqualTo(id);
//                List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(exampleRP);
//                logger.debug("deleteRole: delete role-privilege num = " + rolePrivilegePos.size());
//                for (RolePrivilegePo rolePrivilegePo : rolePrivilegePos) {
//                    rolePrivilegePoMapper.deleteByPrimaryKey(rolePrivilegePo.getId());
//                }
//                //删除缓存中角色权限信息
//                redisTemplate.delete(String.format(ROLEKEY, id));
//                //删除用户角色表
//                UserRolePoExample exampleUR = new UserRolePoExample();
//                UserRolePoExample.Criteria criteriaUR = exampleUR.createCriteria();
//                criteriaUR.andRoleIdEqualTo(id);
//                List<UserRolePo> userRolePos = userRolePoMapper.selectByExample(exampleUR);
//                logger.debug("deleteRole: delete user-role num = " + userRolePos.size());
//                for (UserRolePo userRolePo : userRolePos) {
//                    userRolePoMapper.deleteByPrimaryKey(userRolePo.getId());
//                    //删除缓存中具有删除角色的用户权限
//                    redisTemplate.delete("u_" + userRolePo.getUserId());
//                    redisTemplate.delete("up_" + userRolePo.getUserId());
//                    //查询当前所有有效的代理具有删除角色用户的代理用户
//                    UserProxyPoExample example = new UserProxyPoExample();
//                    UserProxyPoExample.Criteria criteria = example.createCriteria();
//                    criteria.andProxyUserIdEqualTo(userRolePo.getUserId());
//                    List<UserProxyPo> userProxyPos = userProxyPoMapper.selectByExample(example);
//                    for(UserProxyPo userProxyPo : userProxyPos){
//                        //删除缓存中代理了具有删除角色的用户的代理用户
//                        redisTemplate.delete("u_" + userProxyPo.getUserId());
//                        redisTemplate.delete("up_" + userProxyPo.getUserId());
//                    }
//                }
//                retObj = new ReturnObject<>();
//            }

            return retObj;
//        }
//        catch (DataAccessException e){
//            logger.error("selectAllRole: DataAccessException:" + e.getMessage());
//            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
//        }
//        catch (Exception e) {
//            // 其他Exception错误
//            logger.error("other exception : " + e.getMessage());
//            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
//        }
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
     * 由Role Id, Privilege Id 增加 角色权限
     *
     * @param  roleid, Privilegeid, userid
     * @return RolePrivilegeRetVo
     * created by 王琛 24320182203277
     */
    public ReturnObject<VoObject> addPrivByRoleIdAndPrivId(Long roleid, Long privid, Long userid){
        return null;
//        UserPo userpo = userMapper.selectByPrimaryKey(userid);
//        PrivilegePo privilegepo = privilegePoMapper.selectByPrimaryKey(privid);
//        RolePo rolePo = roleMapper.selectByPrimaryKey(roleid);
//        if(userpo==null || privilegepo==null || rolePo==null){
//            return new ReturnObject<VoObject>(ReturnNo.RESOURCE_ID_NOTEXIST);
//        }
//
//        ReturnObject<Role> retObj = null;
//        //获取当前时间
//        LocalDateTime localDateTime = LocalDateTime.now();
//        RolePrivilege rolePrivilege = new RolePrivilege();
//
//        //查询是否角色已经存在此权限
//        RolePrivilegePoExample example = new RolePrivilegePoExample();
//        RolePrivilegePoExample.Criteria criteria = example.createCriteria();
//        criteria.andPrivilegeIdEqualTo(privid);
//        criteria.andRoleIdEqualTo(roleid);
//        List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(example);
//        RolePrivilegePo roleprivilegepo = new RolePrivilegePo();
//
//        if(rolePrivilegePos.isEmpty()){
//            roleprivilegepo.setRoleId(roleid);
//            roleprivilegepo.setPrivilegeId(privid);
//            roleprivilegepo.setCreatorId(userid);
//            roleprivilegepo.setGmtCreate(localDateTime);
//
//            StringBuilder signature = Common.concatString("-", roleprivilegepo.getRoleId().toString(),
//                    roleprivilegepo.getPrivilegeId().toString(), roleprivilegepo.getCreatorId().toString(), localDateTime.toString());
//            String newSignature = SHA256.getSHA256(signature.toString());
//            roleprivilegepo.setSignature(newSignature);
//
//            try {
//                int ret = rolePrivilegePoMapper.insert(roleprivilegepo);
//
//                if (ret == 0) {
//                    //插入失败
//                    return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
//                } else {
//                    //插入成功
//                    //清除角色权限
//                    String key = String.format(ROLEKEY, roleid);
//                    if(redisTemplate.hasKey(key)){
//                        redisTemplate.delete(key);
//                    }
//                }
//            }catch (DataAccessException e){
//                // 数据库错误
//                return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
//            }catch (Exception e) {
//                // 其他Exception错误
//                return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了错误：%s", e.getMessage()));
//            }
//
//        }else{
////            FIELD_NOTVALID
//            return new ReturnObject<>(ReturnNo.FIELD_NOTVALID, String.format("角色权限已存在"));
//        }
//
//        //组装返回的bo
//        rolePrivilege.setId(roleprivilegepo.getId());
//        rolePrivilege.setCreator(userpo);
//        rolePrivilege.setRole(rolePo);
//        rolePrivilege.setPrivilege(privilegepo);
//        rolePrivilege.setGmtModified(localDateTime.toString());
//
//        return new ReturnObject<VoObject>(rolePrivilege);
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
//        RolePrivilegePo rolePrivilegePo = rolePrivilegePoMapper.selectByPrimaryKey(id);
//        int ret = rolePrivilegePoMapper.deleteByPrimaryKey(id);
//        if(ret==0){
//            retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
//        }else{
//            Long roleid = rolePrivilegePo.getRoleId();
//            String key = String.format(ROLEKEY, roleid);
//            //清除缓存被删除的的角色,重新load
//            if(redisTemplate.hasKey(key)){
//                redisTemplate.delete(key);
//            }
//            retObj = new ReturnObject<>();
//        }

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

        ReturnObject returnObject = privDao.getPrivIdsByRoleId(id);
        if(returnObject.getCode()!=ReturnNo.OK){
            return null;
        }
        List<Long> privids = (List<Long>) returnObject.getData();

//        for(Long pid: privids){
//            example.clear();
//            criteria.andRoleIdEqualTo(id);
//            criteria.andPrivilegeIdEqualTo(pid);
//            List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(example);
//            if(rolePrivilegePos!=null && rolePrivilegePos.size()>0 && rolePrivilegePos.get(0)!=null){
//
//                UserPo userpo = userMapper.selectByPrimaryKey(rolePrivilegePos.get(0).getCreatorId());
//                PrivilegePo privilegepo = privilegePoMapper.selectByPrimaryKey(pid);
//
//                //组装权限bo
//                e.setCreator(userpo);
//                e.setId(pid);
//                e.setPrivilege(privilegepo);
//                e.setRole(rolePo);
//                e.setGmtModified(rolePrivilegePos.get(0).getGmtCreate().toString());
//
//                rolepribilegere.add(e);
//            }
//        }
//
//        return new ReturnObject<>(rolepribilegere);
        return null;
    }

    /**
     * 获得用户的角色id
     *
     * @param id 用户id
     * @return 角色id列表
     * createdBy: Ming Qiu 2020/11/3 13:55
     */
    public ReturnObject getRoleIdByUserId(Long id) {
        try{
            UserRolePoExample example = new UserRolePoExample();
            UserRolePoExample.Criteria criteria = example.createCriteria();
            criteria.andUserIdEqualTo(id);
            List<UserRolePo> userRolePoList = userRolePoMapper.selectByExample(example);
            logger.debug("getRoleIdByUserId: userId = " + id + "roleNum = " + userRolePoList.size());
            List<UserRolePo>userRolePosDecoded = Common.listDecode(userRolePoList, UserRolePo.class,baseCoder,null,newUserRoleSignFields,"signature",false);
            List<Long> retIds = new ArrayList<>();
            for (UserRolePo po : userRolePosDecoded) {
                retIds.add(po.getRoleId());
            }
            return new ReturnObject(retIds);
        }catch(Exception e){
            logger.error("getRoleIdByUserId:"+e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }


    /**
     * 由Group Id 获得 Role Id 列表
     * @param id: Group id
     * @return Role Id 列表
     * created by RenJie Zheng 22920192204334
     */
    public ReturnObject getRoleIdsByGroupId(Long id) {
        try{
            GroupRolePoExample example = new GroupRolePoExample();
            GroupRolePoExample.Criteria criteria = example.createCriteria();
            criteria.andGroupIdEqualTo(id);
            List<GroupRolePo> groupRolePoList = groupRolePoMapper.selectByExample(example);
            logger.debug("getRoleIdsByGroupId: groupId = " + id + "roleNum = " + groupRolePoList.size());
            List<GroupRolePo>groupRolePosDecoded = Common.listDecode(groupRolePoList,GroupRolePo.class,baseCoder,null,newGroupRoleSignFields,"signature",false);
            List<Long> retIds = new ArrayList<>();
            for (GroupRolePo po : groupRolePosDecoded) {
                retIds.add(po.getRoleId());
            }
            return new ReturnObject(retIds);
        }catch (Exception e){
            logger.error("getRoleIdsByGroupId:"+e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }

    /**
     * 重写签名和加密
     * @author Ming Qiu
     * date： 2021/12/04 16:01
     */
    public void initialize(){
        //初始化UserRole
        UserRolePoExample example3 = new UserRolePoExample();
        List<UserRolePo> userRolePoList = userRolePoMapper.selectByExample(example3);
        for (UserRolePo po : userRolePoList) {
            UserRolePo newUserRolePo = (UserRolePo) baseCoder.code_sign(po, UserRolePo.class,null,newUserRoleSignFields,"signature");
            logger.debug("initialize: newUserRolePo = "+newUserRolePo.toString());
            userRolePoMapper.updateByPrimaryKeySelective(newUserRolePo);
        }

        //初始化GroupRole
        GroupRolePoExample example = new GroupRolePoExample();
        List<GroupRolePo> groupRolePoList = groupRolePoMapper.selectByExample(example);
        for (GroupRolePo po: groupRolePoList){
            GroupRolePo newPo = (GroupRolePo) baseCoder.code_sign(po, GroupRolePo.class, null, newGroupRoleSignFields, "signature");
            logger.debug("initialize: groupRolePo = "+newPo.toString());
            groupRolePoMapper.updateByPrimaryKeySelective(newPo);
        }

        //初始化RoleInheritance
        RoleInheritedPoExample example2 = new RoleInheritedPoExample();
        List<RoleInheritedPo> roleInheritedPos = roleInheritedPoMapper.selectByExample(example2);
        for (RoleInheritedPo po: roleInheritedPos){
            RoleInheritedPo newPo = (RoleInheritedPo) baseCoder.code_sign(po, RoleInheritedPo.class, null, newRoleInheritedSignFields, "signature");
            logger.debug("initialize: RoleInheritedPo = "+newPo.toString());
            roleInheritedPoMapper.updateByPrimaryKeySelective(newPo);
        }

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
        if (rolePo.getDepartId() != departid) {
            return false;
        }
        return true;
    }

    /**
     * 角色的影响力分析
     * 任务3-6
     * 删除和禁用，修改角色的继承关系时，返回所有影响的rediskey
     *
     * @param roleId 角色id
     * @return 影响的role，group和user的redisKey
     */
    public Collection<String> roleImpact(Long roleId){
        Set<String> impactList=new HashSet<String>();
        getRoleAndRelactiveKey(roleId,impactList);
        return impactList;
    }
    public void getRoleAndRelactiveKey(Long roleId, Set<String> resultSet){
        resultSet.add(String.format(ROLEKEY,roleId));
        List<GroupRolePo> gList=groupRoleDao.selectByRoleId(roleId).getData();
        for(GroupRolePo groupRolePo:gList){
            Collection list=groupDao.groupImpact(groupRolePo.getGroupId());
            resultSet.addAll(list);
        }
        List<UserRolePo> uList=userRoleDao.selectByRoleId(roleId).getData();
        for(UserRolePo userRolePo:uList){
            Collection list=userDao.userImpact(userRolePo.getUserId());
            resultSet.addAll(list);
        }
        RoleInheritedPoExample example=new RoleInheritedPoExample();
        RoleInheritedPoExample.Criteria criteria=example.createCriteria();
        criteria.andRoleIdEqualTo(roleId);
        List<RoleInheritedPo> roleList=roleInheritedPoMapper.selectByExample(example);
        for(RoleInheritedPo roleInheritedPo:roleList){
            if(!resultSet.contains(String.format(ROLEKEY,roleInheritedPo.getRoleCId()))){
                getRoleAndRelactiveKey(roleInheritedPo.getRoleCId(),resultSet);
            }
        }
    }
}
