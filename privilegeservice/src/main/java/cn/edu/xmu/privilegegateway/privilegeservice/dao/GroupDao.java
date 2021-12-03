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
import java.util.List;

@Repository
public class GroupDao {

    private static final Logger logger = LoggerFactory.getLogger(GroupDao.class);

    @Autowired
    private UserGroupPoMapper userGroupPoMapper;

    public final static String GROUPKEY="g_%d";


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
        return null;
    }
}
