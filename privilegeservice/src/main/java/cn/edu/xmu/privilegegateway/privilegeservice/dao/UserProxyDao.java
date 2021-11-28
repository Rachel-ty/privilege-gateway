package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseSign;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.UserPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.UserProxyPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.UserProxy;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserProxyPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserProxyPoExample;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

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
    @Autowired
    private BaseCoder baseCoder;
    protected BaseSign baseSign= new BaseSign() {
        @Override
        protected String encrypt(String content) {
            return null;
        }
    };
    final static List<String> signFields = new ArrayList<>(Arrays.asList("userId", "proxyUserId", "beginDate", "endDate", "valid"));
    final static Collection<String> codeFields = new ArrayList<>();

    public ReturnObject setUsersProxy(UserProxy bo) {
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
            if (!(user.getDepartId().equals(proxyUser.getDepartId()))) {
                return new ReturnObject<>(ReturnNo.USERPROXY_DEPART_CONFLICT);
            }
            bo.setValid((byte) 0);
            UserProxyPo userProxyPo = (UserProxyPo) baseCoder.code_sign(bo, UserProxyPo.class, codeFields, signFields, "signature");
            userProxyPoMapper.insert(userProxyPo);
            UserProxy userProxy = (UserProxy) Common.cloneVo(userProxyPo, UserProxy.class);
            userProxy.setSign((byte)0);
            return new ReturnObject<>(userProxy);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }

    public ReturnObject removeUserProxy(Long id, Long userId) {
        UserProxyPoExample userProxyPoExample = new UserProxyPoExample();
        UserProxyPoExample.Criteria criteria = userProxyPoExample.createCriteria();
        criteria.andIdEqualTo(id);
        criteria.andProxyUserIdEqualTo(userId);
        try {
            int ret = userProxyPoMapper.deleteByExample(userProxyPoExample);
            if (ret == 1) {
                return new ReturnObject<>();
            }
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
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
            PageInfo pageInfo = new PageInfo<>(results);
            ReturnObject pageRetVo = Common.getPageRetVo(new ReturnObject<>(pageInfo), UserProxy.class);
            Map<String,Object> data = (Map<String, Object>) pageRetVo.getData();
            List<UserProxy> list = (List<UserProxy>) data.get("list");
            boolean flag=true;
            for (UserProxy userProxy : list) {
                if (baseSign.check(userProxy, signFields, userProxy.getSignature())) {
                    userProxy.setSign((byte)0);
                }else {
                    userProxy.setSign((byte)1);
                    flag=false;
                }
            }
            data.put("list",list);
            if(flag){
                return new ReturnObject(data);
            }else {
                return new ReturnObject(ReturnNo.RESOURCE_FALSIFY,data);
            }
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
            if (ret == 1) {
                return new ReturnObject();
            }
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
    }

    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public boolean isExistProxy(UserProxy bo) {
        boolean isExist = false;
        UserProxyPoExample example = new UserProxyPoExample();
        UserProxyPoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(bo.getUserId());
        criteria.andProxyUserIdEqualTo(bo.getProxyUserId());
        List<UserProxyPo> results = userProxyPoMapper.selectByExample(example);
        if (!results.isEmpty()) {
            LocalDateTime nowBeginDate = bo.getBeginDate();
            LocalDateTime nowEndDate = bo.getEndDate();
            for (UserProxyPo po : results) {
                LocalDateTime beginDate = po.getBeginDate();
                LocalDateTime endDate = po.getEndDate();
                if ((nowBeginDate.isAfter(beginDate) && nowBeginDate.isBefore(endDate)) || (nowEndDate.isAfter(beginDate) && nowEndDate.isBefore(endDate))) {
                    isExist = true;
                    break;
                }
            }
        }
        return isExist;
    }
}
