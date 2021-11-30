package cn.edu.xmu.privilegegateway.privilegeservice.service;

import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.User;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserProxyRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserProxyVo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserProxyDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.UserProxy;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserSimpleRetVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户服务
 *
 * @author Di Han Li
 * Modified by 24320182203221 李狄翰 at 2020/11/8 8:00
 * Modified by 22920192204222 郎秀晨 at 2021/11/25
 **/
@Service
public class UserProxyService {

    private Logger logger = LoggerFactory.getLogger(UserProxyService.class);

    @Autowired
    private UserProxyDao userProxyDao;

    @Autowired
    private UserDao userDao;

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject setUsersProxy(Long proxyUserId, Long userId, UserProxyVo vo, Long departId, Long creatorId, String creratorName) {
        UserProxy bo = (UserProxy) Common.cloneVo(vo, UserProxy.class);
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
        UserProxyRetVo userProxyRetVo = (UserProxyRetVo) Common.cloneVo(userProxy, UserProxyRetVo.class);
        ReturnObject<User> user = userDao.getUserById(userProxy.getUserId());
        ReturnObject<User> proxyUser = userDao.getUserById(userProxy.getProxyUserId());
//        userProxyRetVo.setUser(new UserSimpleRetVo(user.getData().getId(), user.getData().getName()));
//        userProxyRetVo.setProxyUser(new UserSimpleRetVo(proxyUser.getData().getId(), proxyUser.getData().getName()));
        return new ReturnObject(userProxyRetVo);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject removeUserProxy(Long id, Long userId) {
        return userProxyDao.removeUserProxy(id, userId);
    }

    @Transactional(readOnly = true,rollbackFor = Exception.class)
    public ReturnObject getProxies(Long userId, Long proxyUserId, Long departId, Integer page, Integer pageSize) {
        ReturnObject proxies = userProxyDao.getProxies(userId, proxyUserId, departId, page, pageSize);
        if (proxies.getData() == null) {
            return proxies;
        }
        Map<String, Object> data = (Map<String, Object>) proxies.getData();
        List<UserProxy> userProxyRetVoList = (List<UserProxy>) data.get("list");
        List<UserProxyRetVo> userProxyRetVos = new ArrayList<>();
        for (UserProxy userProxy : userProxyRetVoList) {
            UserProxyRetVo userProxyRetVo = (UserProxyRetVo) Common.cloneVo(userProxy, UserProxyRetVo.class);
            ReturnObject<User> user = userDao.getUserById(userProxy.getUserId());
            ReturnObject<User> proxyUser = userDao.getUserById(userProxy.getProxyUserId());
//            userProxyRetVo.setUser(new UserSimpleRetVo(user.getData().getId(), user.getData().getName()));
//            userProxyRetVo.setProxyUser(new UserSimpleRetVo(proxyUser.getData().getId(), proxyUser.getData().getName()));
            userProxyRetVos.add(userProxyRetVo);
        }
        data.put("list", userProxyRetVos);
        if(proxies.getCode()== ReturnNo.OK){
            return new ReturnObject(data);
        }
        return new ReturnObject(proxies.getCode(),data);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject removeAllProxies(Long id, Long departId) {
        return userProxyDao.removeAllProxies(id, departId);
    }
}

