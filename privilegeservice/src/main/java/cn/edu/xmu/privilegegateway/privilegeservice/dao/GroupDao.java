package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.annotation.util.coder.imp.SHA256Sign;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.GroupPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.GroupRelationPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.*;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.UserGroupPoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private RoleDao roleDao;

    @Autowired
    GroupPoMapper groupPoMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BaseCoder baseCoder;

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
            List<UserGroupPo> userGroupPosDecoded = Common.listDecode(userGroupPoList,UserGroupPo.class,baseCoder,null,newUserGroupSignFields,"signature");
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
            List<GroupRelationPo>ret = (List<GroupRelationPo>) Common.listDecode(groupRelationPos,GroupRelationPo.class,baseCoder,null,newGroupSignFields,"signature");
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
                redisUtil.unionAndStoreSet(pKeys,gKey);
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
}
