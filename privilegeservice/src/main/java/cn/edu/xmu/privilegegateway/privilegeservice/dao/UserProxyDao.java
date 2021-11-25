package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.privilegeservice.mapper.UserPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.UserProxyPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.UserProxy;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserProxyPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserProxyPoExample;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.encript.SHA256;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Di Han Li
 * @date Created in 2020/11/4 9:08
 * Modified by 24320182203221 李狄翰 at 2020/11/8 8:00
 * Modified by 22920192204222 郎秀晨 at 2021/11/25
 **/
@Repository
public class UserProxyDao {

    private static final Logger logger = LoggerFactory.getLogger(UserProxyDao.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private UserProxyPoMapper userProxyPoMapper;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private UserPoMapper userPoMapper;

    public ReturnObject setUsersProxy(UserProxy bo) {
        if (bo.getUserId().equals(bo.getProxyUserId())) {
            return new ReturnObject<>(ReturnNo.USERPROXY_SELF);
        }
        try {
            if (isExistProxy(bo)) {
                return new ReturnObject<>(ReturnNo.USERPROXY_CONFLICT);
            }
            //防止填写部门错误
            UserPo user = userPoMapper.selectByPrimaryKey(bo.getUserId());
            UserPo proxyUser = userPoMapper.selectByPrimaryKey(bo.getProxyUserId());
            if (user == null || proxyUser == null) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            if (!(user.getDepartId().equals(bo.getDepartId()) || (proxyUser.getDepartId().equals(bo.getDepartId())))) {
                return new ReturnObject<>(ReturnNo.USERPROXY_DEPART_CONFLICT);
            }
            UserProxyPo userProxyPo = (UserProxyPo) Common.cloneVo(bo, UserProxyPo.class);
            userProxyPo.setValid((byte) 0);
            StringBuilder signature = Common.concatString("-", userProxyPo.getUserId().toString(), userProxyPo.getProxyUserId().toString(), userProxyPo.getBeginDate().toString(), userProxyPo.getEndDate().toString(), userProxyPo.getValid().toString());
            userProxyPo.setSignature(SHA256.getSHA256(signature.toString()));
            userProxyPoMapper.insert(userProxyPo);
            UserProxy userProxy = (UserProxy) Common.cloneVo(userProxyPo, UserProxy.class);
            return new ReturnObject<>(userProxy);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }

    public ReturnObject removeUserProxy(Long id, Long userId) {
        UserProxyPo userProxyPo = userProxyPoMapper.selectByPrimaryKey(id);
        if (userProxyPo == null) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        if (userId.compareTo(userProxyPo.getProxyUserId()) == 0) {
            try {
                userProxyPoMapper.deleteByPrimaryKey(id);
                return new ReturnObject<>();
            } catch (Exception e) {
                logger.error(e.getMessage());
                return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
            }
        } else {
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
        }
    }

    public ReturnObject getProxies(Long userId, Long proxyUserId, Long departId, Integer page, Integer pageSize) {
        UserProxyPoExample example = new UserProxyPoExample();
        UserProxyPoExample.Criteria criteria = example.createCriteria();
        if (userId != null) {
            criteria.andUserIdEqualTo(userId);
        }
        if (proxyUserId != null) {
            criteria.andProxyUserIdEqualTo(proxyUserId);
        }
        criteria.andDepartIdEqualTo(departId);
        PageHelper.startPage(page, pageSize);
        try {
            List<UserProxyPo> results = userProxyPoMapper.selectByExample(example);
            for (UserProxyPo po : results) {
                StringBuilder signature = Common.concatString("-", po.getUserId().toString(), po.getProxyUserId().toString(), po.getBeginDate().toString(), po.getEndDate().toString(), po.getValid().toString());
                if (!(SHA256.getSHA256(signature.toString()).equals(po.getSignature()))) {
                    StringBuilder message = new StringBuilder().append("listProxies: ").append(ReturnNo.RESOURCE_FALSIFY.getMessage()).append(" id = ")
                            .append(po.getId());
                    logger.error(message.toString());
                    return new ReturnObject<>(ReturnNo.RESOURCE_FALSIFY);
                }
            }
            PageInfo pageInfo = new PageInfo<>(results);
            ReturnObject pageRetVo = Common.getPageRetVo(new ReturnObject<>(pageInfo), UserProxy.class);
            return pageRetVo;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }

    public ReturnObject removeAllProxies(Long id, Long departId) {
        UserProxyPoExample userProxyPoExample = new UserProxyPoExample();
        UserProxyPoExample.Criteria criteria = userProxyPoExample.createCriteria();
        criteria.andDepartIdEqualTo(departId);
        criteria.andIdEqualTo(id);
        try {
            int ret = userProxyPoMapper.deleteByExample(userProxyPoExample);
            if (ret == 0) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            return new ReturnObject();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }
    @Transactional(readOnly = true)
    public boolean isExistProxy(UserProxy bo) {
        boolean isExist = false;
        UserProxyPoExample example = new UserProxyPoExample();
        UserProxyPoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(bo.getUserId());
        criteria.andProxyUserIdEqualTo(bo.getProxyUserId());
        List<UserProxyPo> results = userProxyPoMapper.selectByExample(example);
        if (results != null && results.size() > 0) {
            LocalDateTime nowBeginDate = bo.getBeginDate();
            LocalDateTime nowEndDate = bo.getEndDate();
            for (UserProxyPo po : results) {
                LocalDateTime beginDate = po.getBeginDate();
                LocalDateTime endDate = po.getEndDate();
                //判断开始时间和失效时间是不是不在同一个区间里面
                if (nowBeginDate.equals(beginDate) || nowBeginDate.equals(endDate) || (nowBeginDate.isAfter(beginDate) && nowBeginDate.isBefore(endDate))) {
                    isExist = true;
                    break;
                }
                if (nowEndDate.equals(beginDate) || nowEndDate.equals(endDate) || (nowEndDate.isAfter(beginDate) && nowEndDate.isBefore(endDate))) {
                    isExist = true;
                    break;
                }
            }
        }
        return isExist;
    }
}
