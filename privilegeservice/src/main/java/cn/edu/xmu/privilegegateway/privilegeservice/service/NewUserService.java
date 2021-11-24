package cn.edu.xmu.privilegegateway.privilegeservice.service;

import cn.edu.xmu.privilegegateway.privilegeservice.model.po.NewUserPo;
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
     */
    @Transactional
    public ReturnObject approveUser(boolean approve, Long id) {
        ReturnObject returnObject = null;
        if (approve == true ) {
            NewUserPo newUserPo = newUserDao.findNewUserById(id);
            returnObject = userDao.addUser(newUserPo);
            newUserDao.physicallyDeleteUser(id);
        }
        else if (approve == false ) {
            returnObject=newUserDao.physicallyDeleteUser(id);
        }
        return returnObject;
    }

}
