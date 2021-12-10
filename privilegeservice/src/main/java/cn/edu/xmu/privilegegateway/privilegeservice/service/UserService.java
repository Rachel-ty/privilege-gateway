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

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.PrivilegeDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.RoleDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.User;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.UserBo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.UserRole;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.*;
import cn.edu.xmu.privilegegateway.annotation.util.*;
import cn.edu.xmu.privilegegateway.annotation.util.encript.AES;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
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
    private RoleDao roleDao;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 用户的redis key： u_id
     *
     */
    private final static String USERKEY = "u_%d";

    /**
     * 最终用户的redis key: up_id
     */
    private final static String USERPROXYKEY = "up_%d";

    private final static int INTERNALERROR = 500;

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

    @Autowired
    private BaseCoder baseCoder;


    final static List<String> userSignFields = new ArrayList<>(Arrays.asList("password", "mobile", "email","name","idNumber",
            "passportNumber"));
    final static Collection<String> userCodeFields = new ArrayList<>(Arrays.asList("password", "mobile", "email","name","idNumber",
            "passportNumber"));

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
     * @Modifier 张晖婧
     * */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<VoObject> revokeRole(Long userid, Long roleid, Long did){
        return userDao.revokeRole(userid, roleid,did);
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
        if ((userDao.checkUserDid(userid, did) && roleDao.checkRoleDid(roleid, did)) || did == Long.valueOf(0)) {
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
     * 登录
     * @param userName: 用户名
     * @param password: 密码
     * @param ipAddr: ip地址
     * @return
     * modified by Ming Qiu 2021-11-21 19:34
     *   将redisTemplate 替换成redisUtil
     * modified by RenJie Zheng 22920192204334
     *   添加注释、添加对用户组的处理
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject login(String userName, String password, String ipAddr)
    {
        //获得user对象
        ReturnObject retObj = userDao.getUserByName(userName);
        if (retObj.getCode() != ReturnNo.OK){
            return retObj;
        }

        //进行密码验证、userBo已解密
        UserBo user = (UserBo) retObj.getData();
        //对user对象进行检验
        if(user == null || !password.equals(user.getPassword())){
            retObj = new ReturnObject<>(ReturnNo.AUTH_INVALID_ACCOUNT);
            return retObj;
        }

        //检验成功,将redis中的旧JWT剔除，禁止持有旧JWT的用户登录
        String key = String.format(USERPROXYKEY,  user.getId());
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
            banJwt(jwt);
        }

        //创建新的token
        JwtHelper jwtHelper = new JwtHelper();
        String jwt = jwtHelper.createToken(user.getId(),user.getUserName(),user.getDepartId(), user.getLevel(),jwtExpireTime);
        ReturnObject returnObject = userDao.loadUserPriv(user.getId(), jwt);
        if(returnObject.getData()!= ReturnNo.OK){
            return returnObject;
        }
        logger.debug("login: newJwt = "+ jwt);
        userDao.setLoginIPAndPosition(user.getId(),ipAddr, LocalDateTime.now());
        retObj = new ReturnObject<>(jwt);

        return retObj;
    }

    /**
     * 禁止持有特定令牌的用户登录
     * 任务3-8
     * 修改成lua脚本
     * @param jwt JWT令牌
     */
    /**
     * 用lua脚本重写该方法
     * @author Jianjian Chan
     * @date 2021/12/02
     */
    private void banJwt(String jwt) {
        String[] banSetName = {"BanJwt_0", "BanJwt_1"};
        String banIndexKey = "banIndex";
        String scriptPath = "scripts/ban-jwt.lua";

        DefaultRedisScript<Void> script = new DefaultRedisScript<>();

        script.setScriptSource(new ResourceScriptSource(new ClassPathResource(scriptPath)));
        script.setResultType(Void.class);

        List<String> keyList = new ArrayList<>(List.of(banSetName));
        keyList.add(banIndexKey);

        redisTemplate.execute(script, keyList, banSetName.length, jwt, jwtExpireTime);
    }



    /**
     * 用户登出
     * @param userId 用户id
     * @return
     * modifiedBy 22920192204334 RenJieZheng
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<Boolean> Logout(Long userId)
    {
        String key = String.format(USERPROXYKEY,userId);
        redisUtil.del(key);
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
    public ReturnObject<Object> modifyUserInfo(Long did,Long id, ModifyUserVo vo,Long loginUser,String loginName) {
            return userDao.modifyUserByVo(did,id, vo,loginUser,loginName);
    }
    /**
     * auth009 业务: 将用户踢出
     * @param id 用户 id
     * @return 返回对象 ReturnObject
     * created by 22920192204219 蒋欣雨 at 2021/12/2
     */
    @Transactional
    public void kickOutUser(Long id) {

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
            kickOutUser(id);
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
            kickOutUser(id);
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
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     */
    @Transactional
    public ReturnObject<Object> resetPassword(ResetPwdVo vo) {
        return userDao.resetPassword(vo);
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

        return userDao.changeUserDepart(id,did,loginUser,loginName);
    }




    /**
     * 获取用户状态
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject getUserStates() {
        return userDao.getUserState();
    }

    /**
     * 查看单个用户信息
     * @param id
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject showUserInformation(Long id,Long did){
        UserPo userPo= userDao.findUserById(id);
        if (null==userPo){
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        if(did!=null){
            if (!did.equals(userPo.getDepartId())){
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
            }
        }
        UserBo userBo =( UserBo) baseCoder.decode_check(userPo,UserBo.class,userCodeFields,userSignFields,"signature");
        UserRetVo userRetVo = (UserRetVo) Common.cloneVo(userBo,UserRetVo.class);
        userRetVo.setSign(userBo!=null?0:1);
        UserPo creatorPo = userDao.findUserById(userPo.getCreatorId());
        if (null==creatorPo){
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        UserBo creator = (UserBo) baseCoder.decode_check(creatorPo,UserBo.class,userCodeFields,userSignFields,"signature");
        UserSimpleRetVo creatorSimpleRetVo = new UserSimpleRetVo(creator.getId(),creator.getUserName(),creator!=null?0:1);
        userRetVo.setCreator(creatorSimpleRetVo);
        if(userPo.getModifierId()!=null){
            UserPo modifierPo = userDao.findUserById(userPo.getModifierId());
            if (null==modifierPo){
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            UserBo modifier = (UserBo) baseCoder.decode_check(modifierPo,UserBo.class,userCodeFields,userSignFields,"signature");
            UserSimpleRetVo modifierSimpleRetVo = new UserSimpleRetVo(modifier.getId(),modifier.getUserName(),modifier!=null?0:1);
            userRetVo.setModifier(modifierSimpleRetVo);
        }
        return new ReturnObject(userRetVo);
    }

    /**
     * 修改用户信息
     * @param id
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    @Transactional( rollbackFor = Exception.class)
    public ReturnObject modifyUserInformation(Long id,UserInformationVo userInformationVo,Long userId, String userName){
        UserPo userPo= userDao.findUserById(id);
        if (userPo==null){
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        UserBo userBo = (UserBo)Common.cloneVo(userInformationVo,UserBo.class);
        userBo.setId(id);
        Common.setPoModifiedFields(userBo,userId,userName);
        ReturnObject ret = userDao.modifyUser(userBo);
        return ret;
    }

    /**
     * 查看所有用户
     * @param did
     * @param userName
     * @param mobile
     * @param email
     * @param page
     * @param pageSize
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject<PageInfo<Object>> showUsers(Long did, String userName, String mobile, String email, Integer page, Integer pageSize){
        ReturnObject ret = userDao.selectAllUsers(did,userName,mobile,email,page,pageSize);
        if(ret.getCode()!=ReturnNo.OK){
            return ret;
        }
        List<UserPo> userPos = (List<UserPo>) ret.getData();
        List<Object> userRetVos = new ArrayList<>();
        for (UserPo userPo:userPos){
            UserBo userBo = (UserBo) baseCoder.decode_check(userPo,UserBo.class,userCodeFields,userSignFields,"signature");
            UserRetVo userRetVo = (UserRetVo) Common.cloneVo(userBo,UserRetVo.class);
            userRetVo.setSign(userBo!=null?0:1);
            UserPo creatorPo = userDao.findUserById(userPo.getCreatorId());
            if (null==creatorPo){
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            UserBo creator = (UserBo) baseCoder.decode_check(creatorPo,UserBo.class,userCodeFields,userSignFields,"signature");
            UserSimpleRetVo creatorSimpleRetVo = new UserSimpleRetVo(creator.getId(),creator.getUserName(),creator!=null?0:1);
            userRetVo.setCreator(creatorSimpleRetVo);
            if(userPo.getModifierId()!=null){
                UserPo modifierPo = userDao.findUserById(userPo.getModifierId());
                if (null==modifierPo){
                    return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
                }
                UserBo modifier = (UserBo) baseCoder.decode_check(modifierPo,UserBo.class,userCodeFields,userSignFields,"signature");
                UserSimpleRetVo modifierSimpleRetVo = new UserSimpleRetVo(modifier.getId(),modifier.getUserName(),modifier!=null?0:1);
                userRetVo.setModifier(modifierSimpleRetVo);
            }
            userRetVos.add(userRetVo);
        }
        PageInfo<Object> proxyRetVoPageInfo = PageInfo.of(userRetVos);
        return new ReturnObject(proxyRetVoPageInfo);
    }


    /**
     * 获取用户名
     * @param id
     * @return
     * @author BingShuai Liu 22920192204245
     */
    @Transactional( rollbackFor = Exception.class)
    public ReturnObject showUserName(Long id){
        UserPo userPo= userDao.findUserById(id);
        if (userPo==null){
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        UserBo userBo = (UserBo) baseCoder.decode_check(userPo,UserBo.class,userCodeFields,userSignFields,"signature");
        if(null==userBo){
            return new ReturnObject(ReturnNo.RESOURCE_FALSIFY);
        }

        ReturnObject ret = new ReturnObject(userBo.getUserName());
        return ret;
    }

    /**
     * 上传头像
     * @param id
     * @param multipartFile
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    @Transactional
    public ReturnObject uploadNewImg(Long id, MultipartFile multipartFile){
        UserPo userPo= userDao.findUserById(id);
        if (userPo==null){
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        UserBo user = (UserBo) baseCoder.decode_check(userPo,UserBo.class,userCodeFields,userSignFields,"signature");
        ReturnObject returnObject = new ReturnObject();
        try{
            returnObject = ImgHelper.remoteSaveImg(multipartFile,100000,davUsername, davPassword,baseUrl);

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
     * 查看任意用户的角色
     *
     * @author 22920192204289 王文凯
     * @param userId 用户id
     * @param page 页数
     * @param pageSize 每页大小
     * @return Object 角色返回视图
     * createdBy 王文凯 2021/11/26 11:44
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectRoles(Long userId, Long did, Integer page, Integer pageSize) {
        return userDao.selectRoles(userId, did, page, pageSize);
    }

    /**
     * 查看自己的角色
     *
     * @author 22920192204289 王文凯
     * @param page 页数
     * @param pageSize 每页大小
     * @return Object 角色返回视图
     * createdBy 王文凯 2021/11/26 11:44
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectSelfRoles(Long userId, Long did, Integer page, Integer pageSize) {
        return userDao.selectRoles(userId, did, page, pageSize);
    }

    /**
     * 查看自己的功能角色
     *
     * @author 22920192204289 王文凯
     * @param page 页数
     * @param pageSize 每页大小
     * @return Object 角色返回视图
     * createdBy 王文凯 2021/11/26 11:44
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectSelfBaseRoles(Long userId, Long did, Integer page, Integer pageSize) {
        return userDao.selectBaseRoles(userId, did, page, pageSize);
    }

    /**
     * 获得用户的功能角色
     *
     * @author 22920192204320 张晖婧
     * @param did: 部门 id
     * @param id: 用户 id
     * @return Object
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject findBaserolesByUserId(Long did, Long id, Integer page, Integer pageSize) {
        return userDao.selectBaseRoles( id,did, page, pageSize);
    }
    /**
     * 内部api-将某个用户的权限信息装载到Redis中
     * @param userId: 用户 id
     * @return Object 装载的用户id
     * @author RenJie Zheng 22920192204334
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject loadUserPrivilege(Long userId,String jwt){
        return userDao.loadUserPriv(userId,jwt);
    }


    /**
     * 赋予用户角色
     * @param userid 用户id
     * @param roleid 角色id
     * @param did departid
     * @return UserRoleRetVo
     * @author 张晖婧
     * */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<VoObject> assignRole(Long creatorId, String creatorName, Long userid, Long roleid, Long did) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userid);
        userRole.setRoleId(roleid);
        Common.setPoCreatedFields(userRole, creatorId, creatorName);
        Common.setPoModifiedFields(userRole, creatorId, creatorName);

        ReturnObject returnObject = userDao.assignRole(userRole, did);

        return Common.getRetVo(returnObject,UserRoleSimpleRetVo.class);
    }
}
