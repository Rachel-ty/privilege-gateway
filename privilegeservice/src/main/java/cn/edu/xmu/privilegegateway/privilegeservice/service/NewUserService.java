package cn.edu.xmu.privilegegateway.privilegeservice.service;

import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.NewUserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.ApproveConclusionVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.NewUserVo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.NewUserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * @param vo 注册的vo对象
     * @return ReturnObject
     * @author LiangJi3229
     */
    @Transactional
    public ReturnObject register(NewUserVo vo) {
        return newUserDao.createNewUserByVo(vo);
    }
    
    /**
     * 管理员审核用户
     * @param id
     * @param approve
     * @return ReturnObject
     * @author 24320182203227 Li Zihan
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional
    public ReturnObject approveUser(ApproveConclusionVo approve, Long did,Long id,Long loginUser,String loginName) {

        ReturnObject returnObject = null;
        if ((newUserDao.checkUserDid(id, did) || did == Long.valueOf(0))) {
            if (approve.getApprove()) {
                NewUserPo newUserPo = newUserDao.findNewUserById(id);
                returnObject = userDao.addUser(newUserPo,loginUser,loginName);
                newUserDao.physicallyDeleteUser(id);
            }
            else {
                returnObject=newUserDao.physicallyDeleteUser(id);
            }
        }
        else {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        return returnObject;

    }

}
