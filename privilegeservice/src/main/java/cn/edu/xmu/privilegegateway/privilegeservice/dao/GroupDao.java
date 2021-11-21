package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.privilegeservice.mapper.UserGroupPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserGroupPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserGroupPoExample;
import cn.edu.xmu.privilegegateway.util.Common;
import cn.edu.xmu.privilegegateway.util.encript.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class GroupDao {

    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

    @Autowired
    private UserGroupPoMapper userGroupPoMapper;


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
}
