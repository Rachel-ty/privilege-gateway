package cn.edu.xmu.privilegegateway.privilegeservice.service;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.PrivilegeDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.User;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.ModifyPwdVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.ResetPwdVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserVo;
import cn.edu.xmu.privilegegateway.annotation.util.*;
import cn.edu.xmu.privilegegateway.annotation.util.encript.AES;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户服务
 * @author Ming Qiu
 * Modified at 2020/11/5 10:39
 **/
@Service
public class UserService {

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${privilegeservice.login.jwtExpire}")
    private Integer jwtExpireTime;

    /**
     * @author 24320182203218
     **/

    @Value("${privilegeservice.dav.username}")
    private String davUsername;

    @Value("${privilegeservice.dav.password}")
    private String davPassword;

    @Value("${privilegeservice.dav.baseUrl}")
    private String baseUrl;

    @Autowired
    private PrivilegeDao privilegeDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisUtil redisUtil;
    /**
     * 最终用户的redis key: up_id
     */
    private final static String USERPROXYKEY = "up_%d";
    /**
     * @author yue hao
     * 根据用户Id查询用户所有权限
     * @param id:用户id
     * @return 用户权限列表
     */
    public ReturnObject<List> findPrivsByUserId(Long id, Long did){
        return userDao.findPrivsByUserId(id,did);
    }

    @Value("${privilegeservice.login.multiply}")
    private Boolean canMultiplyLogin;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    /**
     * 分布式锁的过期时间（秒）
     */
    @Value("${privilegeservice.lockerExpireTime}")
    private long lockerExpireTime;

    /**
     * ID获取用户信息
     * @author XQChen
     * @param id
     * @return 用户
     */
    public ReturnObject<VoObject> findUserById(Long id) {
        ReturnObject<VoObject> returnObject = null;

        UserPo userPo = userDao.findUserById(id);
        if(userPo != null) {
            logger.debug("findUserById : " + returnObject);
            returnObject = new ReturnObject<>(new User(userPo));
        } else {
            logger.debug("findUserById: Not Found");
            returnObject = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        return returnObject;
    }

    /**
     * ID和DID获取用户信息
     * @author XQChen
     * @param id
     * @return 用户
     */
    public ReturnObject<VoObject> findUserByIdAndDid(Long id, Long did) {
        ReturnObject<VoObject> returnObject = null;

        UserPo userPo = userDao.findUserByIdAndDid(id, did);
        if(userPo != null) {
            logger.debug("findUserByIdAndDid : " + returnObject);
            returnObject = new ReturnObject<>(new User(userPo));
        } else {
            logger.debug("findUserByIdAndDid: Not Found");
            returnObject = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        return returnObject;
    }

    /**
     * 获取所有用户信息
     * @author XQChen
     * @param userName
     * @param mobile
     * @param page
     * @param pagesize
     * @return 用户列表
     */
    public ReturnObject<PageInfo<VoObject>> findAllUsers(String userName, String mobile, Integer page, Integer pagesize, Long did) {

        String userNameAES = userName.isBlank() ? "" : AES.encrypt(userName, User.AESPASS);
        String mobileAES = mobile.isBlank() ? "" : AES.encrypt(mobile, User.AESPASS);

        PageHelper.startPage(page, pagesize);
        PageInfo<UserPo> userPos = userDao.findAllUsers(userNameAES, mobileAES, did);

        List<VoObject> users = userPos.getList().stream().map(User::new).filter(User::authetic).collect(Collectors.toList());

        PageInfo<VoObject> returnObject = new PageInfo<>(users);
        returnObject.setPages(userPos.getPages());
        returnObject.setPageNum(userPos.getPageNum());
        returnObject.setPageSize(userPos.getPageSize());
        returnObject.setTotal(userPos.getTotal());

        return new ReturnObject<>(returnObject);
    }

    /**
     * 取消用户角色
     * @param userid 用户id
     * @param roleid 角色id
     * @param did departid
     * @return ReturnObject<VoObject>
     * @author Xianwei Wang
     * */
    @Transactional
    public ReturnObject<VoObject> revokeRole(Long userid, Long roleid, Long did){
        if ((userDao.checkUserDid(userid, did) && userDao.checkRoleDid(roleid, did)) || did == Long.valueOf(0)) {
            return userDao.revokeRole(userid, roleid);
        } else {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }

    /**
     * 赋予用户角色
     * @param createid 创建者id
     * @param userid 用户id
     * @param roleid 角色id
     * @param did departid
     * @return UserRole
     * @author Xianwei Wang
     * */
    @Transactional
    public ReturnObject<VoObject> assignRole(Long createid, Long userid, Long roleid, Long did){
        if ((userDao.checkUserDid(userid, did) && userDao.checkRoleDid(roleid, did)) || did == Long.valueOf(0)) {
            return userDao.assignRole(createid, userid, roleid);
        }
        else {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }

    /**
     * 查看自己的角色信息
     * @param id 用户id
     * @return 角色信息
     * @author Xianwei Wang
     * */
    @Transactional
    public ReturnObject<List> getSelfUserRoles(Long id){
        return userDao.getUserRoles(id);
    }

    /**
     * 查看角色信息
     * @param id 用户id
     * @param did departid
     * @return 角色信息
     * @author Xianwei Wang
     * */
    @Transactional
    public ReturnObject<List> getUserRoles(Long id, Long did){
        if (userDao.checkUserDid(id, did) || did == Long.valueOf(0)) {
            return userDao.getUserRoles(id);
        } else {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }


    /**
     * 查询所有权限
     * @param page: 页码
     * @param pageSize : 每页数量
     * @return 权限列表
     */
    public ReturnObject<PageInfo<VoObject>> findAllPrivs(Integer page, Integer pageSize){
        return privilegeDao.findAllPrivs(page, pageSize);
    }

    /**
     * 修改权限
     * @param id: 权限id
     * @return
     */
    public ReturnObject changePriv(Long id, PrivilegeVo vo){
        return privilegeDao.changePriv(id, vo);
    }

    /**
     * 登录
     * @param userName: 用户名
     * @param password: 密码
     * @param ipAddr: ip地址
     * @return
     * modified by Ming Qiu 2021-11-21 19:34
     *   将redisTemplate 替换成redisUtil
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject login(String userName, String password, String ipAddr)
    {
        ReturnObject retObj = userDao.getUserByName(userName);
        if (retObj.getCode() != ReturnNo.OK){
            return retObj;
        }

        User user = (User) retObj.getData();
        password = AES.encrypt(password, User.AESPASS);
        if(user == null || !password.equals(user.getPassword())){
            retObj = new ReturnObject<>(ReturnNo.AUTH_INVALID_ACCOUNT);
            return retObj;
        }
        if (user.getState() != User.State.NORM){
            retObj = new ReturnObject<>(ReturnNo.AUTH_USER_FORBIDDEN);
            return retObj;
        }
        if (!user.getEmailVerified()){
            return new ReturnObject<>(ReturnNo.EMAIL_NOTVERIFIED);
        }
        if (!user.getMobileVerified()){
            return new ReturnObject<>(ReturnNo.MOBILE_NOTVERIFIED);
        }
        if (!user.authetic()){
            retObj = new ReturnObject<>(ReturnNo.RESOURCE_FALSIFY, "用户信息被篡改");
            StringBuilder message = new StringBuilder().append("Login: userid = ").append(user.getId()).
                    append(", username =").append(user.getUserName()).append(" 信息被篡改");
            logger.error(message.toString());
            return retObj;
        }

        String key = "up_" + user.getId();
        logger.debug("login: key = "+ key);
        if(redisUtil.hasKey(key) && !canMultiplyLogin){
            logger.debug("login: multiply  login key ="+key);
            // 用户重复登录处理
            Set<Serializable > set = redisUtil.getSet(key);
            redisUtil.del(key);

            /* 将旧JWT加入需要踢出的集合 */
            String jwt = null;
            for (Serializable str : set) {
                /* 找出JWT */
                if((str.toString()).length() > 8){
                    jwt =  str.toString();
                    break;
                }
            }
            logger.debug("login: oldJwt" + jwt);
            this.banJwt(jwt);
        }

        //创建新的token
        JwtHelper jwtHelper = new JwtHelper();
        String jwt = jwtHelper.createToken(user.getId(),user.getUserName(),user.getDepartId(), user.getLevel(),jwtExpireTime);
        userDao.loadUserPriv(user.getId(), jwt);
        logger.debug("login: newJwt = "+ jwt);
        userDao.setLoginIPAndPosition(user.getId(),ipAddr, LocalDateTime.now());
        retObj = new ReturnObject<>(jwt);

        return retObj;
    }

    /**
     * 禁止持有特定令牌的用户登录
     * @param jwt JWT令牌
     */
    private void banJwt(String jwt){
        String[] banSetName = {"BanJwt_0", "BanJwt_1"};
        long bannIndex = 0;
        if (!redisTemplate.hasKey("banIndex")){
            redisTemplate.opsForValue().set("banIndex", Long.valueOf(0));
        } else {
            logger.debug("banJwt: banIndex = " +redisTemplate.opsForValue().get("banIndex"));
            bannIndex = Long.parseLong(redisTemplate.opsForValue().get("banIndex").toString());
        }
        logger.debug("banJwt: banIndex = " + bannIndex);
        String currentSetName = banSetName[(int) (bannIndex % banSetName.length)];
        logger.debug("banJwt: currentSetName = " + currentSetName);
        if(!redisTemplate.hasKey(currentSetName)) {
            // 新建
            logger.debug("banJwt: create ban set" + currentSetName);
            redisTemplate.opsForSet().add(currentSetName, jwt);
            redisTemplate.expire(currentSetName,jwtExpireTime * 2,TimeUnit.SECONDS);
        }else{
            //准备向其中添加元素
            if(redisTemplate.getExpire(currentSetName, TimeUnit.SECONDS) > jwtExpireTime) {
                // 有效期还长，直接加入
                logger.debug("banJwt: add to exist ban set" + currentSetName);
                redisTemplate.opsForSet().add(currentSetName, jwt);
            } else {
                // 有效期不够JWT的过期时间，准备用第二集合，让第一个集合自然过期
                // 分步式加锁
                logger.debug("banJwt: switch to next ban set" + currentSetName);
                long newBanIndex = bannIndex;
                while (newBanIndex == bannIndex &&
                        !redisTemplate.opsForValue().setIfAbsent("banIndexLocker","nouse", lockerExpireTime, TimeUnit.SECONDS)){
                    //如果BanIndex没被其他线程改变，且锁获取不到
                    try {
                        Thread.sleep(10);
                        //重新获得新的BanIndex
                        newBanIndex = (Long) redisTemplate.opsForValue().get("banIndex");
                    }catch (InterruptedException e){
                        logger.error("banJwt: 锁等待被打断");
                    }
                    catch (IllegalArgumentException e){

                    }
                }
                if (newBanIndex == bannIndex) {
                    //切换ban set
                    bannIndex = redisTemplate.opsForValue().increment("banIndex");
                }else{
                    //已经被其他线程改变
                    bannIndex = newBanIndex;
                }

                currentSetName = banSetName[(int) (bannIndex % banSetName.length)];
                //启用之前，不管有没有，先删除一下，应该是没有，保险起见
                redisTemplate.delete(currentSetName);
                logger.debug("banJwt: next ban set =" + currentSetName);
                redisTemplate.opsForSet().add(currentSetName, jwt);
                redisTemplate.expire(currentSetName,jwtExpireTime * 2,TimeUnit.SECONDS);
                // 解锁
                redisTemplate.delete("banIndexLocker");
            }
        }
    }


    public ReturnObject<Boolean> Logout(Long userId)
    {
        redisTemplate.delete("up_" + userId);
        return new ReturnObject<>(true);
    }

    /**
     * auth009 业务: 根据 ID 和 UserEditVo 修改任意用户信息
     * @param id 用户 id
     * @param vo UserEditVo 对象
     * @return 返回对象 ReturnObject
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional
    public ReturnObject<Object> modifyUserInfo(Long did,Long id, UserVo vo,Long loginUser,String loginName) {
            return userDao.modifyUserByVo(did,id, vo,loginUser,loginName);
    }

    /**
     * auth009 业务: 根据 id 删除任意用户
     * @param id 用户 id
     * @return 返回对象 ReturnObject
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional
    public ReturnObject<Object> deleteUser(Long did,Long id,Long loginUser,String loginName) {

            // 注：逻辑删除
            ReturnObject ret=userDao.changeUserState(did,id,loginUser,loginName ,User.State.DELETE);

            String key =  String.format(USERPROXYKEY, id);
            logger.debug("login: key = "+ key);
            if(redisUtil.hasKey(key)){
                Set<Serializable > set = redisUtil.getSet(key);
                redisUtil.del(key);
                /* 将旧JWT加入需要踢出的集合 */
                String jwt = null;
                for (Serializable str : set) {
                    /* 找出JWT */
                    if((str.toString()).length() > 8){
                        jwt =  str.toString();
                        break;
                    }
                }
                logger.debug("login: oldJwt" + jwt);
                this.banJwt(jwt);
            }
            return ret;
    }

    /**
     * auth009 业务: 根据 id 禁止任意用户登录
     * @param id 用户 id
     * @return 返回对象 ReturnObject
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional
    public ReturnObject<Object> forbidUser(Long did,Long id,Long loginUser,String loginName) {

            ReturnObject ret=userDao.changeUserState(did,id,loginUser,loginName, User.State.FORBID);
            String key =  String.format(USERPROXYKEY, id);
            logger.debug("login: key = "+ key);
            if(redisUtil.hasKey(key)){
                Set<Serializable > set = redisUtil.getSet(key);
                redisUtil.del(key);
                /* 将旧JWT加入需要踢出的集合 */
                String jwt = null;
                for (Serializable str : set) {
                    /* 找出JWT */
                    if((str.toString()).length() > 8){
                        jwt =  str.toString();
                        break;
                    }
                }
                logger.debug("login: oldJwt" + jwt);
                this.banJwt(jwt);
            }
            return ret;
    }

    /**
     * auth009 业务: 解禁任意用户
     * @param id 用户 id
     * @return 返回对象 ReturnObject
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional
    public ReturnObject<Object> releaseUser(Long did,Long id,Long loginUser,String loginName) {
            return userDao.changeUserState(did,id,loginUser,loginName, User.State.NORM);

    }

    /**
     * 上传图片
     * @author 3218
     * @param id: 用户id
     * @param multipartFile: 文件
     * @return
     */
    @Transactional
    public ReturnObject uploadImg(Long id, MultipartFile multipartFile){
        ReturnObject<User> userReturnObject = userDao.getUserById(id);

        if(userReturnObject.getCode() == ReturnNo.RESOURCE_ID_NOTEXIST) {
            return userReturnObject;
        }
        User user = userReturnObject.getData();

        ReturnObject returnObject = new ReturnObject();
        try{
            returnObject = ImgHelper.remoteSaveImg(multipartFile,2,davUsername, davPassword,baseUrl);

            //文件上传错误
            if(returnObject.getCode()!=ReturnNo.OK){
                logger.debug(returnObject.getErrmsg());
                return returnObject;
            }

            String oldFilename = user.getAvatar();
            user.setAvatar(returnObject.getData().toString());
            ReturnObject updateReturnObject = userDao.updateUserAvatar(user);

            //数据库更新失败，需删除新增的图片
            if(updateReturnObject.getCode()==ReturnNo.FIELD_NOTVALID){
                ImgHelper.deleteRemoteImg(returnObject.getData().toString(),davUsername, davPassword,baseUrl);
                return updateReturnObject;
            }

            //数据库更新成功需删除旧图片，未设置则不删除
            if(oldFilename!=null) {
                ImgHelper.deleteRemoteImg(oldFilename, davUsername, davPassword,baseUrl);
            }
        }
        catch (IOException e){
            logger.debug("uploadImg: I/O Error:" + baseUrl);
            return new ReturnObject(ReturnNo.FILE_NO_WRITE_PERMISSION);
        }
        return returnObject;
    }

        /**
     * auth002: 用户重置密码
     * @param vo 重置密码对象
     * @param ip 请求ip地址
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     */
    @Transactional
    public ReturnObject<Object> resetPassword(ResetPwdVo vo, String ip) {
        return userDao.resetPassword(vo,ip);
    }

    /**
     * auth002: 用户修改密码
     * @param vo 修改密码对象
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     */
    @Transactional
    public ReturnObject<Object> modifyPassword(ModifyPwdVo vo) {
        return userDao.modifyPassword(vo);
    }

    /**
     * 业务: 将用户加入部门
     * @param id 用户 id
     * @return 返回对象 InternalReturnObject
     * Created by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional
    public  InternalReturnObject<Object> addToDepart(Long did,Long id,Long loginUser,String loginName) {

        return userDao.addUserToDepart(id,did,loginUser,loginName);
    }
    
    
}
