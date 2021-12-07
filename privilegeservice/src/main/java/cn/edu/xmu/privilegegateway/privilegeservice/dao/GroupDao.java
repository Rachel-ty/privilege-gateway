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

import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.GroupPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.GroupRelationPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.GroupRolePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.*;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.*;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.UserGroupPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.*;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.GroupRelationVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.RetGroup;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserGroup;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserRelation;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Repository
public class GroupDao {

    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

    @Autowired
    private UserGroupPoMapper userGroupPoMapper;

    @Autowired
    private GroupRelationPoMapper groupRelationPoMapper;

    @Autowired
    private GroupPoMapper groupPoMapper;

    @Autowired
    private GroupRolePoMapper groupRolePoMapper;

    @Autowired @Lazy
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BaseCoder baseCoder;
    private Collection<String> codeFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile"));
    private List<String> signFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile","state","departId","level"));
    private Collection<String> groupRelationCodeFields = new ArrayList<>(Arrays.asList());
    private List<String> groupRelationSignFields = new ArrayList<>(Arrays.asList("groupPId","groupSId"));
    private Collection<String> groupRoleCodeFields = new ArrayList<>(Arrays.asList());
    private List<String> groupRoleSignFields = new ArrayList<>(Arrays.asList("roleId","groupId"));
    private Collection<String> groupUserCodeFields = new ArrayList<>(Arrays.asList());
    private List<String> groupUserSignFields = new ArrayList<>(Arrays.asList("userId","groupId"));

    final static List<String> newGroupSignFields = new ArrayList<>(Arrays.asList("groupPId", "groupSId"));

    final static List<String> newUserGroupSignFields = new ArrayList<>(Arrays.asList("userId", "groupId"));

    // 用户在Redis中的过期时间,用户组相同
    @Value("${privilegeservice.user.expiretime}")
    private long timeout;

    /**
     * 用户的redis key： u_id
     *
     */
    private final static String USERKEY = "u_%d";
    /**
     * 用户组的redis key： g_id
     *
     */
    private final static String GROUPKEY = "g_%d";

    private final static String ROLEKEY = "r_%d";

    private final static int BANED = 2;



    /**
     * 获得用户的组id
     * @param id 用户id
     * @return 组id列表
     * createdBy:  Ming Qiu 2021-11-21 19:34
     * modifiedBy: RenJieZheng 22920192204334
     *      添加try catch和returnObject
     */
    public ReturnObject getGroupIdByUserId(Long id) {
        try{
            UserGroupPoExample example = new UserGroupPoExample();
            UserGroupPoExample.Criteria criteria = example.createCriteria();
            criteria.andUserIdEqualTo(id);
            List<UserGroupPo> userGroupPoList = userGroupPoMapper.selectByExample(example);
            logger.debug("getGroupIdByUserId: userId = " + id + "groupNum = " + userGroupPoList.size());
            List<UserGroupPo> userGroupPosDecoded = Common.listDecode(userGroupPoList,UserGroupPo.class,baseCoder,null,newUserGroupSignFields,"signature",false);
            List<Long> retIds = new ArrayList<>();
            for (UserGroupPo po : userGroupPosDecoded) {
                retIds.add(po.getGroupId());
            }
            return new ReturnObject<>(retIds);
        }catch(Exception e){
            logger.error("getGroupIdByUserId: "+e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }

    /**
     * 根据groupId获得上级groupIds
     * @param groupId groupId
     * @return 上级gorupIds
     * CreateBy RenJieZheng 22920192204334
     */
    public ReturnObject getParentGroup(Long groupId){
        try{
            GroupRelationPoExample example = new GroupRelationPoExample();
            GroupRelationPoExample.Criteria criteria = example.createCriteria();
            criteria.andGroupSIdEqualTo(groupId);
            List<GroupRelationPo>groupRelationPos = groupRelationPoMapper.selectByExample(example);
            logger.debug("getSuperiorGroupIdsByGroupId: groupId = " + groupId);
            List<GroupRelationPo>ret = (List<GroupRelationPo>) Common.listDecode(groupRelationPos,GroupRelationPo.class,baseCoder,null,newGroupSignFields,"signature",false);
            return new ReturnObject(ret);
        }catch (Exception e){
            logger.error("getSuperiorGroupIdsByGroupId: "+e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }

    /**
     * 计算当前group的功能角色，load到Redis。
     * @param groupId groupId
     * CreateBy RenJieZheng 22920192204334
     */
    public ReturnObject loadSingleGroup(Long groupId){
        try{
            String gKey = String.format(GROUPKEY,groupId);
            ReturnObject returnObject = roleDao.getRoleIdsByGroupId(groupId);
            if(returnObject.getCode()!=ReturnNo.OK){
                return returnObject;
            }
            List<Long> roleIds = (List<Long>) returnObject.getData();
            Set<String> roleKeys = new HashSet<>(roleIds.size());
            for (Long roleId : roleIds) {
                String roleKey = String.format(ROLEKEY, roleId);
                if (!redisUtil.hasKey(roleKey)) {
                    ReturnObject returnObject1 = roleDao.loadRole(roleId);
                    if(returnObject1.getCode()!=ReturnNo.OK){
                        return returnObject1;
                    }
                }
                roleKeys.add(roleKey);
            }
            if(roleKeys.size()>0){
                redisUtil.unionAndStoreSet(roleKeys, gKey);
            }
            redisUtil.addSet(gKey,0);
            long randTimeout = Common.addRandomTime(timeout);
            redisUtil.expire(gKey, randTimeout, TimeUnit.SECONDS);
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e){
            logger.error("loadSingleGroup:"+e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }


    /**
     * 计算当前group的功能角色，load到Redis。此groupId的权限包含本身权限和所有它祖先节点的权限
     * @param groupId groupId
     * CreateBy RenJieZheng 22920192204334
     */
    public ReturnObject loadGroup(Long groupId){
        try{
            String gKey = String.format(GROUPKEY, groupId);
            List<String>pKeys = new ArrayList<>();
            GroupPo groupPo = groupPoMapper.selectByPrimaryKey(groupId);

            //用户组被禁止
            if(groupPo.getState()!=null&&groupPo.getState()==BANED){
                redisUtil.addSet(gKey,0);
                return new ReturnObject();
            }


            // 计算本身功能角色
            ReturnObject returnObject = loadSingleGroup(groupId);
            if(returnObject.getCode()!=ReturnNo.OK){
                return returnObject;
            }
            // 计算所有父节点的功能角色
            ReturnObject returnObject2 = getParentGroup(groupId);
            if(returnObject2.getCode()!=ReturnNo.OK){
                return returnObject2;
            }
            List<GroupRelationPo>groupRelationRootPos = (List<GroupRelationPo>) returnObject2.getData();

            //没有父节点说明是根节点
            if(groupRelationRootPos.size()<=0){
                loadSingleGroup(groupId);
                return new ReturnObject(ReturnNo.OK);
            }

            for(GroupRelationPo groupRelationPo:groupRelationRootPos){
                if(!redisUtil.hasKey(String.format(GROUPKEY,groupRelationPo.getGroupPId()))){
                    //计算当前父节点
                    ReturnObject returnObject3 = loadGroup(groupRelationPo.getGroupPId());
                    if(returnObject3.getCode()!=ReturnNo.OK){
                        return returnObject3;
                    }
                }
                pKeys.add(String.format(GROUPKEY,groupRelationPo.getGroupPId()));
            }
            if(pKeys.size()>0){
                redisUtil.unionAndStoreSet(gKey,pKeys,gKey);
            }
            redisUtil.addSet(gKey,0);
            long randTimeout = Common.addRandomTime(timeout);
            redisUtil.expire(gKey, randTimeout, TimeUnit.SECONDS);
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e){
            logger.error("loadGroup: "+e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }

    public void initialize(){
        //初始化UserGroup
        UserGroupPoExample example = new UserGroupPoExample();
        List<UserGroupPo> userGroupPoList = userGroupPoMapper.selectByExample(example);
        for (UserGroupPo po : userGroupPoList) {
            UserGroupPo newUserRolePo = (UserGroupPo) baseCoder.code_sign(po, UserGroupPo.class,null,newUserGroupSignFields,"signature");
            userGroupPoMapper.updateByPrimaryKeySelective(newUserRolePo);
        }

        //初始化GroupRelation
        GroupRelationPoExample example1 = new GroupRelationPoExample();
        List<GroupRelationPo> groupRelationPos = groupRelationPoMapper.selectByExample(example1);
        for (GroupRelationPo po: groupRelationPos){
            GroupRelationPo newPo = (GroupRelationPo) baseCoder.code_sign(po, GroupRelationPo.class, null, newGroupSignFields, "signature");
            groupRelationPoMapper.updateByPrimaryKeySelective(newPo);
        }

    }

    /**
     * 组的影响力分析
     * 任务3-5
     * 删除和禁用某个权限时，返回所有影响的group和user的redisKey
     * @param groupId 组id
     * @return 
     * @author BingShuai Liu 22920192204245
     */
    public Collection<String> groupImpact(Long groupId){
        Collection<String> keys = new ArrayList<>();
        HashSet<Long> groupIds = new HashSet<>();
        HashSet<Long> userIds = new HashSet<>();
        getAllGroups(groupId,groupIds);
        groupIds.add(groupId);
        for (Long gId: groupIds){
            String gKey= String.format(GROUPKEY,gId);
            keys.add(gKey);
            UserGroupPoExample example = new UserGroupPoExample();
            UserGroupPoExample.Criteria criteria = example.createCriteria();
            criteria.andGroupIdEqualTo(gId);
            List<UserGroupPo> userGroupPos = userGroupPoMapper.selectByExample(example);
            for (UserGroupPo userGroupPo: userGroupPos){
                Collection<String> uKeys = userDao.userImpact(userGroupPo.getUserId());
                if (userIds.add(userGroupPo.getUserId())){
                    String uKey = String.format(UserDao.USERKEY,userGroupPo.getUserId());
                    keys.add(uKey);
                }
                for (String uKey : uKeys){
                    String id = uKey.substring(UserDao.USERKEY.length()-2);
                    if (userIds.add(Long.parseLong(id))){
                        keys.add(uKey);
                    }
                }
            }
        }
        return keys;
    }

    /**
     * @author BingShuai Liu 22920192204245
    */
    public void getAllGroups(Long groupId,HashSet<Long> groupIds){
        GroupRelationPoExample example = new GroupRelationPoExample();
        GroupRelationPoExample.Criteria criteria = example.createCriteria();
        criteria.andGroupPIdEqualTo(groupId);
        List<GroupRelationPo> groupRelationPos = groupRelationPoMapper.selectByExample(example);
        if(groupRelationPos==null||groupRelationPos.size()==0){
            return;
        }else{
            for (GroupRelationPo groupRelationPo : groupRelationPos){
                groupIds.add(groupRelationPo.getGroupSId());
                getAllGroups(groupRelationPo.getGroupSId(),groupIds);
            }
        }
    }

    /**
     * 获得所有部门的组
     * @param did
     * @param page
     * @param pageSize
     * @return
     * createdBy:  Weining Shi
     */

    public ReturnObject<PageInfo<RetGroup>> getGroupsBydid(Long did, Integer page, Integer pageSize) {
        GroupPoExample example=new GroupPoExample();
        GroupPoExample.Criteria criteria = example.createCriteria();
        if(did!=null)
            criteria.andDepartIdEqualTo(did);
        try {
            PageHelper.startPage(page, pageSize);
            List<GroupPo> groupPos = groupPoMapper.selectByExample(example);////////////////////////////////
            PageInfo pageInfo = new PageInfo(groupPos);
            ReturnObject pageRetVo = Common.getPageRetVo(new ReturnObject<>(pageInfo), RetGroup.class);
            Map<String,Object> data = (Map<String, Object>) pageRetVo.getData();
            return new ReturnObject(data);
        } catch (Exception e) {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }

    /**
     * 增加一个用户组
     *
     * @param group bo
     * @return ReturnObject<Group> 新增结果
     * createdBy:  Weining Shi
     */
    public ReturnObject<RetGroup> insertGroup(Group group, Long loginUserId, String loginUserName) {
        GroupPo groupPo = (GroupPo) Common.cloneVo(group,GroupPo.class);
        ReturnObject<RetGroup> retObj = null;
        try{
            Common.setPoCreatedFields(groupPo,loginUserId,loginUserName);
            int ret = groupPoMapper.insertSelective(groupPo);//////////////////////////////////////////
            RetGroup retGroup = (RetGroup) Common.cloneVo(groupPo,RetGroup.class);
            retGroup.setSign((byte) 0);
            retObj = new ReturnObject<>(retGroup);
        }
        catch (DataAccessException e) {
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了内部错误：%s", e.getMessage()));
        }
        return retObj;
    }
    /**
     *修改用户组的信息
     * @param id
     * @param group
     * @return
     * createdBy:  Weining Shi
     */


    public ReturnObject<RetGroup> updateGroup(Long id, Group group,Long loginUserId, String loginUserName) {
        int ret=0;

        Collection<String> ids;
        RetGroup temp=new RetGroup();
        try {
            GroupPo groupPo = groupPoMapper.selectByPrimaryKey(id);
            groupPo.setName(group.getName());
            if(group.getState()!=null) {
                groupPo.setState(group.getState());
                ids = groupImpact(groupPo.getId());
            }

            Common.setPoModifiedFields(groupPo,loginUserId,loginUserName);
            ret = groupPoMapper.updateByPrimaryKeySelective(groupPo);
            temp=(RetGroup) Common.cloneVo(groupPo,RetGroup.class);
            temp.setSign((byte) (0));
        }
        catch (Exception e) {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR);
        }
        if (ret == 0) {
            return new ReturnObject(ReturnNo.FIELD_NOTVALID,"更新失败");
        } else {
            return new ReturnObject(temp);
        }

    }

    /**
     * 获得用户组的所有状态
     * @return
     * createdBy:  Weining Shi
     */

    public ReturnObject<List<Map<String, Object>>> getAllStates() {
        List<Map<String, Object>> stateList = new ArrayList<>();
        for (Group.State states : Group.State.values()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("code", states.getCode());
            temp.put("name", states.getDescription());
            stateList.add(temp);
        }
        return new ReturnObject<>(stateList);
    }

    /**
     * 通过id获得组
     * @param id
     * @return
     * createdBy:  Weining Shi
     */
    public ReturnObject<Group> getGroupByid(Long id) {
        try {
            GroupPo po = groupPoMapper.selectByPrimaryKey(id);
            if (po == null) {
                return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            Group ret = (Group) Common.cloneVo(po, Group.class);
            return new ReturnObject<>(ret);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 删除用户组（级联）
     * @param id
     * @return
     * createdBy:  Weining Shi
     */

    public ReturnObject deleteGroup(Long id) {

        Collection<String> ids=groupImpact(id);

        List<GroupRelationPo> relationPos = new ArrayList<>();
        try{
            GroupPo groupPo = groupPoMapper.selectByPrimaryKey(id);
            //所有的父子关系
            GroupRelationPoExample relationPoExample = new GroupRelationPoExample();
            GroupRelationPoExample.Criteria criteria = relationPoExample.createCriteria();
            criteria.andGroupSIdEqualTo(groupPo.getId());
            GroupRelationPoExample.Criteria criteria1 = relationPoExample.createCriteria();
            criteria1.andGroupPIdEqualTo(groupPo.getId());
            relationPoExample.or(criteria1);
            groupRelationPoMapper.deleteByExample(relationPoExample);
            //所有用户关系
            UserGroupPoExample userGroupPoExample = new UserGroupPoExample();
            UserGroupPoExample.Criteria criteria2 = userGroupPoExample.createCriteria();
            criteria2.andGroupIdEqualTo(id);
            userGroupPoMapper.deleteByExample(userGroupPoExample);
            //所有角色关系
            GroupRolePoExample groupRolePoExample = new GroupRolePoExample();
            GroupRolePoExample.Criteria criteria3 = groupRolePoExample.createCriteria();
            criteria3.andGroupIdEqualTo(id);
            groupRolePoMapper.deleteByExample(groupRolePoExample);
            groupPoMapper.deleteByPrimaryKey(id);
            for(String key:ids)
            {
                if(redisUtil.hasKey(key)){
                    redisUtil.del(key);
                }
            }
            return new ReturnObject(ReturnNo.OK);
        }
        catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }

    /**
     *
     * @param sid
     * @param pid
     * @return
     * createdBy:  Weining Shi
     */

    public ReturnObject<List<GroupRelation>> getGroupRelationBypidsid(Long sid, Long pid) {
        GroupRelationPoExample example = new GroupRelationPoExample();
        GroupRelationPoExample.Criteria criteria = example.createCriteria();
        if(pid!=null)
            criteria.andGroupPIdEqualTo(pid);
        if(sid!=null)
            criteria.andGroupSIdEqualTo(sid);
        List<GroupRelationPo> po;
        List<GroupRelation> bos;
        try {
            po = groupRelationPoMapper.selectByExample(example);////////////////////////////////////////////
            logger.debug("getGroupRelationBypidsid: pid = " + pid + "sid = " + sid+" sum = "+po.size());
            bos=Common.listDecode(po, GroupRelation.class,baseCoder,groupRelationCodeFields,groupRelationSignFields,"signature",true);
            int check=0;
            for(GroupRelation bo:bos)
            {
                if (bo.getSignature() == null)
                {
                    bo.setSign((byte) 1);
                    check=1;
                }
                else
                    bo.setSign((byte)0);
            }
            if(check==0)
                return new ReturnObject(bos);
            else
                return new ReturnObject(ReturnNo.RESOURCE_FALSIFY,"签名错误",bos);
        } catch (Exception e) {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR);
        }

    }

    /**
     *
     * @param groupRelation
     * @param loginUserId
     * @param loginUserName
     * @return
     * createdBy:  Weining Shi
     */
    public ReturnObject<GroupRelationVo> addGroupRelation(GroupRelation groupRelation, Long loginUserId, String loginUserName)
    {
        groupImpact(groupRelation.getGroupSId());
        GroupRelationPo groupRelationPo= (GroupRelationPo) Common.cloneVo(groupRelation,GroupRelationPo.class);
        ReturnObject<GroupRelationVo> retObj = null;
        try{
            Common.setPoCreatedFields(groupRelationPo,loginUserId,loginUserName);
            groupRelationPo= (GroupRelationPo) baseCoder.code_sign(groupRelationPo,GroupRelationPo.class,groupRelationCodeFields,groupRelationSignFields,"signature");
            int ret = groupRelationPoMapper.insertSelective(groupRelationPo);//////////////////////////////////////////
            if (ret == 0) {
                retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST, String.format("新增失败：" + groupRelationPo.getGroupPId())+" "+groupRelationPo.getGroupSId());
            } else {
                GroupRelationVo temp=(GroupRelationVo) Common.cloneVo(groupRelationPo,GroupRelationVo.class);
                temp.setSign((byte) 0);
                retObj = new ReturnObject(temp);
            }
        }
        catch (DataAccessException e) {
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了内部错误：%s", e.getMessage()));
        }
        return retObj;
    }

    /**
     *
     * @param sid
     * @param pid
     * @return
     * createdBy:  Weining Shi
     */
    public ReturnObject<List<GroupRelation>> findGroupRelationBypidsid(Long sid, Long pid) {
        GroupRelationPoExample example = new GroupRelationPoExample();
        GroupRelationPoExample.Criteria criteria = example.createCriteria();
        if(pid!=null)
            criteria.andGroupPIdEqualTo(pid);
        if(sid!=null)
            criteria.andGroupSIdEqualTo(sid);
        List<GroupRelationPo> po;
        List<GroupRelation> bos;
        try {
            po = groupRelationPoMapper.selectByExample(example);////////////////////////////////////////////
            logger.debug("getGroupRelationBypidsid: pid = " + pid + "sid = " + sid+" sum = "+po.size());
            bos=Common.listDecode(po, GroupRelation.class,baseCoder,groupRelationCodeFields,groupRelationSignFields,"signature",true);
            int check=0;
            for(GroupRelation bo:bos)
            {
                if (bo.getSignature() == null)
                {
                    bo.setSign((byte) 1);
                    check=1;
                }
                else
                    bo.setSign((byte)0);
            }
            if(check==0)
                return new ReturnObject(bos);
            else
                return new ReturnObject(ReturnNo.RESOURCE_FALSIFY,"签名错误",bos);
        } catch (Exception e) {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }

    /**
     *
     * @param id
     * @param userId
     * @param userName
     * @return
     * createdBy:  Weining Shi
     */
    public ReturnObject deleteGroupRelation(Long id, Long userId, String userName) {
        Collection<String> ids;
        GroupRelationPo groupRelationPo=groupRelationPoMapper.selectByPrimaryKey(id);
        if(groupRelationPo==null)
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        try {
            groupRelationPoMapper.deleteByPrimaryKey(id);
            ids=groupImpact(groupRelationPo.getGroupPId());
            ids.addAll(groupImpact(groupRelationPo.getGroupSId()));
            RetGroup temp=(RetGroup) Common.cloneVo(groupRelationPo,RetGroup.class);
            temp.setSign((byte) 0);
            for(String key:ids)
            {
                if(redisUtil.hasKey(key)){
                    redisUtil.del(key);
                }
            }
            return new ReturnObject(temp);
        } catch (Exception e) {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }

    /**
     *
     * @param uid
     * @param gid
     * @return
     * createdBy:  Weining Shi
     */
    public ReturnObject<List<Pair<UserGroupPo,Byte>>> getUserGroupByuidgid(Long uid, Long gid) {
        UserGroupPoExample example = new UserGroupPoExample();
        UserGroupPoExample.Criteria criteria = example.createCriteria();
        if(uid!=null)
            criteria.andUserIdEqualTo(uid);
        if(gid!=null)
            criteria.andGroupIdEqualTo(gid);
        int flag=0;
        List<UserGroupPo> po;
        try {
            po = userGroupPoMapper.selectByExample(example);////////////////////////////////////////////
        } catch (Exception e) {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR);
        }
        List<Pair<UserGroupPo, Byte>> ret=new ArrayList<>();
        for(UserGroupPo it:po)//校验签名
        {
            if(baseCoder.decode_check(it,it.getClass(),groupUserCodeFields,groupUserSignFields,"signature")==null)
            {
                ret.add(Pair.of(it,(byte)1));
            }
            else
                ret.add(Pair.of(it,(byte)0));
        }

        if(ret!=null)
        {
            if(flag==0)
                return new ReturnObject<>(ret);
            else
                return new ReturnObject<>(ReturnNo.RESOURCE_FALSIFY,"签名错误",ret);
        }
        return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);

    }

    /**
     *
     * @param userGroup
     * @param userId
     * @param userName
     * @return
     * createdBy:  Weining Shi
     */
    public ReturnObject<UserGroup> insertUserGroup(UserGroup userGroup, Long userId, String userName) {
        Collection<String> ids;

        UserGroupPo userGroupPo;
        userGroupPo= (UserGroupPo) baseCoder.code_sign(userGroup,UserGroupPo.class,groupUserCodeFields,groupUserSignFields,"signature");
        ids=userDao.userImpact(userGroupPo.getUserId());
        ReturnObject<UserGroup> retObj = null;
        try{
            Common.setPoCreatedFields(userGroupPo,userId,userName);
            int ret = userGroupPoMapper.insertSelective(userGroupPo);//////////////////////////////////////////
            userGroup.setId(userGroupPo.getId());
            retObj = new ReturnObject<>(userGroup);
        }
        catch (DataAccessException e) {
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了内部错误：%s", e.getMessage()));
        }
        return retObj;

    }

    /**
     *
     * @param id
     * @param userId
     * @param userName
     * @return
     * createdBy:  Weining Shi
     */
    public ReturnObject deleteUserGroup(Long id, Long userId, String userName) {
        Collection<String> ids;

        try {
            UserGroupPo userGroupPo=userGroupPoMapper.selectByPrimaryKey(id);
            if(userGroupPo==null)
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            ids=userDao.userImpact(userGroupPo.getUserId());
            userGroupPoMapper.deleteByPrimaryKey(id);
            for(String key:ids)
            {
                if(redisUtil.hasKey(key)){
                    redisUtil.del(key);
                }
            }
            return new ReturnObject(ReturnNo.OK,"成功");
        } catch (Exception e) {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }

    /**
     *
     * @param did
     * @param id
     * @param page
     * @param pageSize
     * @return
     * createdBy:  Weining Shi
     */
    public ReturnObject<PageInfo<Object>> getusersBygid(Long did, Long id, Integer page, Integer pageSize) {

        UserGroupPoExample example = new UserGroupPoExample();
        UserGroupPoExample.Criteria criteria = example.createCriteria();
        List<UserRelation> users=new ArrayList<>();
        if(id!=null)
            criteria.andGroupIdEqualTo(id);

        List<UserGroupPo> po;
        List<UserRelationBo> bos;
        try {
            PageHelper.startPage(page,pageSize);
            po = userGroupPoMapper.selectByExample(example);////////////////////////////////////////////
            logger.debug("getGroupIdByUserId: userId = " + id + "groupNum = " + po.size());
            bos=Common.listDecode(po,UserRelationBo.class,baseCoder,groupUserCodeFields,groupUserSignFields,"signature",true);
            int check=0;
            for(UserRelationBo bo:bos) {
                if (bo.getSignature() == null)
                {
                    bo.setSign((byte) 1);
                    check=1;
                }
                else
                    bo.setSign((byte)0);
            }
            if(check==1)
                return Common.getPageRetVo(new ReturnObject(ReturnNo.RESOURCE_FALSIFY,"签名错误",new PageInfo<>(bos)),UserRelation.class);
            else
                return Common.getPageRetVo(new ReturnObject(new PageInfo<>(bos)),UserRelation.class);
        } catch (Exception e) {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }
    /**
     * 获得用户的组
     *
     * @param id 用户id
     * @return 组id列表
     * createdBy:  Weining Shi
     */
    public List<UserGroup> getUserGroupByUserId(Long id,Integer page,Integer pageSize) {
        UserGroupPoExample example = new UserGroupPoExample();
        UserGroupPoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(id);
        List<UserGroup> temp=new ArrayList<>();
        List<UserGroupBo> bos=new ArrayList<>();
        try{
            PageHelper.startPage(page, pageSize);
            List<UserGroupPo> userGroupPoList = userGroupPoMapper.selectByExample(example);
            logger.debug("getGroupIdByUserId: userId = " + id + "groupNum = " + userGroupPoList.size());
            bos=Common.listDecode(userGroupPoList, UserGroupBo.class,baseCoder,groupUserCodeFields,groupUserSignFields,"signature",true);
            int check=0;
            for(UserGroupBo bo:bos) {
                if (bo.getSignature() == null)
                {
                    bo.setSign((byte) 1);
                    check=1;
                }
                else
                    bo.setSign((byte)0);
            }
            temp=Common.listDecode(bos,UserGroup.class,baseCoder,null,null,null,true);
            return temp;
        }
        catch (Exception e){
            logger.error("getUserGroupByUserId: id =" + id);
            return temp;
        }
    }
}
