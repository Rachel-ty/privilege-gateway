package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.GroupRelationPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.GroupRelationPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.GroupRelationPoExample;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserGroupPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserGroupPoExample;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.encript.SHA256;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.UserGroupPoMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Repository
public class GroupDao {

    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

    @Autowired
    private UserGroupPoMapper userGroupPoMapper;

    public final static String GROUPKEY="g_%d";

    private final static String USERKEY = "u_%d";

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private GroupRelationPoMapper groupRelationPoMapper;

    @Autowired
    private BaseCoder baseCoder;
    final static List<String> groupRelationSignFields = new ArrayList<>(Arrays.asList("groupPId","groupSId"));
    final static Collection<String> groupRelationCodeFields = new ArrayList<>();
    final static List<String> userGroupSignFields = new ArrayList<>(Arrays.asList("userId","groupId"));
    final static Collection<String> userGroupCodeFields = new ArrayList<>();


    /**
     * 获得用户的组id
     *
     * @param id 用户id
     * @return 组id列表
     * createdBy:  Ming Qiu 2021-11-21 19:34
     */
    private List<Long> getGroupIdByUserId(Long id) {
        UserGroupPoExample example = new UserGroupPoExample();
        UserGroupPoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(id);
        List<UserGroupPo> userGroupPoList = userGroupPoMapper.selectByExample(example);
        logger.debug("getGroupIdByUserId: userId = " + id + "groupNum = " + userGroupPoList.size());
        List<Long> retIds = new ArrayList<>(userGroupPoList.size());
        for (UserGroupPo po : userGroupPoList) {
            StringBuilder signature = Common.concatString("-",
                    po.getUserId().toString(), po.getGroupId().toString());
            String newSignature = SHA256.getSHA256(signature.toString());

            if (newSignature.equals(po.getSignature())) {
                retIds.add(po.getGroupId());
                logger.debug("getGroupIdByUserId: userId = " + po.getUserId() + " roleId = " + po.getGroupId());
            } else {
                logger.error("getGroupIdByUserId: 签名错误(auth_user_group): id =" + po.getId());
            }
        }
        return retIds;
    }

    /**
     * 组的影响力分析
     * 任务3-5
     * 删除和禁用某个权限时，删除所有影响的group和user的redisKey
     * @param groupId 组id
     * @return 影响的group和user的redisKey
     */
    public List<String> groupImpact(Long groupId){
        List<String> keys = new ArrayList<>();
        List<Long> groupIds = new ArrayList<>();
        List<Long> userIds =new ArrayList<>();
        getAllGroups(groupId,groupIds);
        groupIds.add(groupId);
        for (Long gId: groupIds){
            String gKey= String.format(GROUPKEY,gId);
            if(redisUtil.hasKey(gKey)){
                keys.add(gKey);
            }
            UserGroupPoExample example1 = new UserGroupPoExample();
            UserGroupPoExample.Criteria criteria1 = example1.createCriteria();
            criteria1.andGroupIdEqualTo(gId);
            List<UserGroupPo> userGroupPos = userGroupPoMapper.selectByExample(example1);
            for (UserGroupPo userGroupPo: userGroupPos){
                if (!userIds.contains(userGroupPo.getUserId())){
                    userIds.add(userGroupPo.getUserId());
                }
            }
        }
        for(Long uId:userIds){
            String uKey = String.format(UserDao.USERKEY,uId);
            if(redisUtil.hasKey(uKey)){
                keys.add(uKey);
            }
        }
        return keys;
    }

    public void getAllGroups(Long groupId,List<Long> groupIds){
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
}
