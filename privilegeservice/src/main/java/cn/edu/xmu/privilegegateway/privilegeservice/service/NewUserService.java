package cn.edu.xmu.privilegegateway.privilegeservice.service;

import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.NewUserBo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.NewUserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.ConclusionVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.NewUserVo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.NewUserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserSimpleRetVo;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 新用户服务
 * @author LiangJi3229
 * @date 2020/11/10 18:41
 */
@Service
public class NewUserService {
    private Logger logger = LoggerFactory.getLogger(NewUserService.class);

    @Autowired
    NewUserDao newUserDao;

    @Autowired
    UserDao userDao;

    @Autowired
    private BaseCoder baseCoder;
    final static List<String> signFields = new ArrayList<>(Arrays.asList("userName", "password", "mobile", "email","name","idNumber",
            "passportNumber"));
    final static Collection<String> codeFields = new ArrayList<>(Arrays.asList("userName", "password", "mobile", "email","name","idNumber",
            "passportNumber"));


    /**
     * 向newUser表中插入
     * @param newUserVo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject newUser(NewUserVo newUserVo){
        NewUserBo newUserBo = (NewUserBo) Common.cloneVo(newUserVo, NewUserBo.class);
        ReturnObject ret = newUserDao.createNewUserByBo(newUserBo);
        if(ret.getData()==null){
            return ret;
        }
        NewUserPo newUserPo = (NewUserPo)ret.getData();
        UserSimpleRetVo userSimpleRetVo = (UserSimpleRetVo)Common.cloneVo(newUserPo,UserSimpleRetVo.class);
        userSimpleRetVo.setUserName(newUserVo.getUserName());
        userSimpleRetVo.setSign(0);
        return new ReturnObject(userSimpleRetVo);
    }

    /**
     * 获取所有新建用户
     * @param did
     * @param userName
     * @param mobile
     * @param email
     * @param page
     * @param pageSize
     * @return
     * @author BingShuai Liu 22920192204245
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject<PageInfo<Object>> showNewUsers(Long did, String userName, String mobile, String email, Integer page, Integer pageSize){
        ReturnObject ret = newUserDao.selectAllNewUsers(did,userName,mobile,email,page,pageSize);
        if(ret.getCode()!=ReturnNo.OK){
            return ret;
        }
        List<NewUserPo> newUserPos = (List<NewUserPo>) ret.getData();
        List<Object> newUsers = new ArrayList<>();
        for (NewUserPo newUserPo:newUserPos){
            NewUserBo userBo = (NewUserBo) baseCoder.decode_check(newUserPo,NewUserBo.class,codeFields,signFields,"signature");
            UserSimpleRetVo userSimpleRetVo = new UserSimpleRetVo(userBo.getId(),userBo.getUserName(),0);
            newUsers.add(userSimpleRetVo);
        }
        PageInfo<Object> proxyRetVoPageInfo = PageInfo.of(newUsers);
        return new ReturnObject(proxyRetVoPageInfo);
    }

}
