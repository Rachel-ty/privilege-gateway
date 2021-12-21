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

package cn.edu.xmu.privilegegateway.privilegeservice.service;

import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserProxyRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserProxyVo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.UserProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务
 *
 * @author Di Han Li
 * Modified by 24320182203221 李狄翰 at 2020/11/8 8:00
 * Modified by 22920192204222 郎秀晨 at 2021/11/25
 **/
@Service
public class UserProxyService {

    @Autowired
    private UserDao userProxyDao;

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject setUsersProxy(Long proxyUserId, Long userId, UserProxyVo vo, Long departId, Long creatorId, String creratorName) {
        UserProxy bo = Common.cloneVo(vo, UserProxy.class);
        Common.setPoCreatedFields(bo, creatorId, creratorName);
        Common.setPoModifiedFields(bo, creatorId, creratorName);
        bo.setUserId(userId);
        bo.setProxyUserId(proxyUserId);
        bo.setDepartId(departId);
        ReturnObject returnObject = userProxyDao.setUsersProxy(bo);
        if (returnObject.getData() == null) {
            return returnObject;
        }
        UserProxy userProxy = (UserProxy) returnObject.getData();
        UserProxyRetVo userProxyRetVo = Common.cloneVo(userProxy, UserProxyRetVo.class);
        return new ReturnObject(userProxyRetVo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject removeUserProxy(Long id, Long userId) {
        return userProxyDao.removeUserProxy(id, userId);
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject getProxies(Long userId, Long proxyUserId, Long departId, LocalDateTime beginTime, LocalDateTime endTime, Integer page, Integer pageSize) {
        ReturnObject proxies = userProxyDao.getProxies(userId, proxyUserId, departId, beginTime, endTime, page, pageSize);
        return proxies;
    }
    @Transactional(readOnly = true)
    public ReturnObject getProxiesSelf(Long userId,Integer page,Integer pageSize){
        return userProxyDao.getProxiesSelf(userId,page,pageSize);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject removeAllProxies(Long id, Long departId) {
        return userProxyDao.removeAllProxies(id, departId);
    }
}

