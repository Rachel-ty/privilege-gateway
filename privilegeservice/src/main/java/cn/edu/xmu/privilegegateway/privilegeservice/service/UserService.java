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
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户服务
 *
 * @author Ming Qiu
 * Modified at 2020/11/5 10:39
 **/
@Service
public class UserService {

    private Logger logger = LoggerFactory.getLogger(UserService.class);

    @Value("${privilegeservice.login.jwtExpire}")
    private Integer jwtExpireTime;

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
     */
    private final static String USERKEY = "u_%d";

    /**
     * 最终用户的redis key: up_id
     */
    private final static String USERPROXYKEY = "up_%d";

    @Value("${privilegeservice.login.multiply}")
    private Boolean canMultiplyLogin;

    @Autowired
    private BaseCoder baseCoder;


    final static List<String> userSignFields = new ArrayList<>(Arrays.asList("password", "mobile", "email", "name", "idNumber",
            "passportNumber"));
    final static Collection<String> userCodeFields = new ArrayList<>(Arrays.asList("password", "mobile", "email", "name", "idNumber",
            "passportNumber"));


    /**
     * 取消用户角色
     *
     * @param userid 用户id
     * @param roleid 角色id
     * @param did    departid
     * @return ReturnObject<VoObject>
     * @author Xianwei Wang
     * @Modifier 张晖婧
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<VoObject> revokeRole(Long userid, Long roleid, Long did) {
        return userDao.revokeRole(userid, roleid, did);
    }


    /**
     * 登录
     *
     * @param account: 用户名
     * @param password: 密码
     * @param ipAddr:   ip地址
     * @return modified by Ming Qiu 2021-11-21 19:34
     * 将redisTemplate 替换成redisUtil
     * modified by RenJie Zheng 22920192204334
     * 添加注释、添加对用户组的处理
     * Modified By Ming Qiu 2021-12-12 07:44
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject login(String account, String password, String ipAddr) {
        //获得user对象
        ReturnObject retObj = userDao.getUserByAccount(account);
        if (retObj.getCode() != ReturnNo.OK) {
            return retObj;
        }

        //进行密码验证、userBo已解密
        UserBo user = (UserBo) retObj.getData();
        //对user对象进行检验
        if (user == null || !password.equals(user.getPassword())) {
            retObj = new ReturnObject<>(ReturnNo.AUTH_INVALID_ACCOUNT);
            return retObj;
        }

        //检验成功,将redis中的旧JWT剔除，禁止持有旧JWT的用户登录
        String key = String.format(USERPROXYKEY, user.getId());
        logger.debug("login: key = " + key);
        if (redisUtil.hasKey(key) && !canMultiplyLogin) {
            logger.debug("login: multiply  login key =" + key);
            // 用户重复登录处理
            Set<Serializable> set = redisUtil.getSet(key);
            redisUtil.del(key);

            /* 将旧JWT加入需要踢出的集合 */
            String jwt = null;
            for (Serializable str : set) {
                /* 找出JWT */
                if ((str.toString()).length() > 8) {
                    jwt = str.toString();
                    break;
                }
            }
            logger.debug("login: oldJwt" + jwt);
            banJwt(jwt);
        }

        //创建新的token
        JwtHelper jwtHelper = new JwtHelper();
        String jwt = jwtHelper.createToken(user.getId(), user.getUserName(), user.getDepartId(), user.getLevel(), jwtExpireTime);
        ReturnObject returnObject = userDao.loadUserPriv(user.getId(), jwt);
        if (returnObject.getCode() != ReturnNo.OK) {
            return returnObject;
        }
        logger.debug("login: newJwt = " + jwt);
        retObj = userDao.setLoginIPAndPosition(user.getId(), ipAddr, LocalDateTime.now());
        if (retObj.getCode() != ReturnNo.OK) {
            return retObj;
        }
        return new ReturnObject<>(jwt);
    }

    /**
     * 禁止持有特定令牌的用户登录
     * 任务3-8
     * 修改成lua脚本
     * @param jwt JWT令牌
     */
    /**
     * 用lua脚本重写该方法
     *
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

        redisUtil.executeScript(script, keyList, banSetName.length, jwt, jwtExpireTime);
    }


    /**
     * 用户登出
     *
     * @param userId 用户id
     * @return modifiedBy 22920192204334 RenJieZheng
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject Logout(Long userId) {
        String key = String.format(USERPROXYKEY, userId);
        redisUtil.del(key);
        return new ReturnObject(ReturnNo.OK);
    }

    /**
     * auth009 业务: 根据 ID 和 UserEditVo 修改任意用户信息
     *
     * @param id 用户 id
     * @param vo UserEditVo 对象
     * @return 返回对象 ReturnObject
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<Object> modifyUserInfo(Long did, Long id, ModifyUserVo vo, Long loginUser, String loginName) {
        return userDao.modifyUserByVo(did, id, vo, loginUser, loginName);
    }

    /**
     * auth009 业务: 将用户踢出
     *
     * @param id 用户 id
     * @return 返回对象 ReturnObject
     * created by 22920192204219 蒋欣雨 at 2021/12/2
     */
    @Transactional(rollbackFor = Exception.class)
    public void kickOutUser(Long id) {

        String key = String.format(USERPROXYKEY, id);
        logger.debug("login: key = " + key);
        if (redisUtil.hasKey(key)) {
            Set<Serializable> set = redisUtil.getSet(key);
            redisUtil.del(key);
            /* 将旧JWT加入需要踢出的集合 */
            String jwt = null;
            for (Serializable str : set) {
                /* 找出JWT */
                if ((str.toString()).length() > 8) {
                    jwt = str.toString();
                    break;
                }
            }
            logger.debug("login: oldJwt" + jwt);
            this.banJwt(jwt);
        }
    }


    /**
     * auth009 业务: 根据 id 删除任意用户
     *
     * @param id 用户 id
     * @return 返回对象 ReturnObject
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<Object> deleteUser(Long did, Long id, Long loginUser, String loginName) {

        // 注：逻辑删除
        ReturnObject ret = userDao.changeUserState(did, id, loginUser, loginName, User.State.DELETE);
        kickOutUser(id);
        return ret;
    }


    /**
     * auth009 业务: 根据 id 禁止任意用户登录
     *
     * @param id 用户 id
     * @return 返回对象 ReturnObject
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<Object> forbidUser(Long did, Long id, Long loginUser, String loginName) {

        ReturnObject ret = userDao.changeUserState(did, id, loginUser, loginName, User.State.FORBID);
        kickOutUser(id);
        return ret;
    }

    /**
     * auth009 业务: 解禁任意用户
     *
     * @param id 用户 id
     * @return 返回对象 ReturnObject
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<Object> releaseUser(Long did, Long id, Long loginUser, String loginName) {
        return userDao.changeUserState(did, id, loginUser, loginName, User.State.NORM);

    }


    /**
     * auth002: 用户重置密码
     *
     * @param vo 重置密码对象
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<Object> resetPassword(ResetPwdVo vo) {
        return userDao.resetPassword(vo);
    }

    /**
     * auth002: 用户修改密码
     *
     * @param vo 修改密码对象
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<Object> modifyPassword(ModifyPwdVo vo) {
        return userDao.modifyPassword(vo);
    }

    /**
     * 业务: 将用户加入部门
     *
     * @param id 用户 id
     * @return 返回对象 InternalReturnObject
     * Created by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject addToDepart(Long did, Long id, Long loginUser, String loginName) {

        return userDao.changeUserDepart(id, did, loginUser, loginName);
    }


    /**
     * 获取用户状态
     *
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject getUserStates() {
        return userDao.getUserState();
    }

    /**
     * 查看单个用户信息
     *
     * @param id
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject showUserInformation(Long id, Long did) {
        ReturnObject<User> ret1 = userDao.getUserById(id);
        if (ret1.getCode() != ReturnNo.OK) {
            return ret1;
        }
        User user = ret1.getData();
        if(did!=null){
            if (!did.equals(user.getDepartId())) {
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
            }
        }

        UserRetVo userRetVo = Common.cloneVo(user, UserRetVo.class);
        return new ReturnObject(userRetVo);
    }

    /**
     * 修改用户信息
     *
     * @param id
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject modifyUserInformation(Long id, UserInformationVo userInformationVo, Long userId, String userName) {
        User user = (User) Common.cloneVo(userInformationVo, User.class);
        user.setId(id);
        Common.setPoModifiedFields(user, userId, userName);
        ReturnObject ret = userDao.modifyUser(user);
        return ret;
    }

    /**
     * 查看所有用户
     *
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
    public ReturnObject<PageInfo<Object>> showUsers(Long did, String userName, String mobile, String email, Integer page, Integer pageSize) {
        ReturnObject ret = userDao.selectAllUsers(did, userName, mobile, email, page, pageSize);
        if (ret.getCode() != ReturnNo.OK) {
            return ret;
        }
        List<UserPo> userPos = (List<UserPo>) ret.getData();
        List<Object> userRetVos = new ArrayList<>();
        for (UserPo userPo : userPos) {
            UserPo userPo1 = (UserPo) baseCoder.decode_check(userPo, UserPo.class, userCodeFields, userSignFields, "signature");
            UserRetVo userRetVo = Common.cloneVo(userPo1, UserRetVo.class);
            userRetVo.setSign(userPo1.getSignature() != null ? (byte) 0 : (byte) 1);
            userRetVos.add(userRetVo);
        }
        PageInfo<Object> proxyRetVoPageInfo = PageInfo.of(userRetVos);
        return new ReturnObject(proxyRetVoPageInfo);
    }


    /**
     * 获取用户名
     *
     * @param id
     * @return
     * @author BingShuai Liu 22920192204245
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject showUserName(Long id) {
        ReturnObject<User> ret1 = userDao.getUserById(id);
        if (ret1.getCode() != ReturnNo.OK) {
            return ret1;
        }

        User user = ret1.getData();
        ReturnObject ret = new ReturnObject(user.getUserName());
        return ret;
    }

    /**
     * 上传头像
     *
     * @param id
     * @param multipartFile
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject uploadNewImg(Long id, MultipartFile multipartFile) {
        ReturnObject<User> ret1 = userDao.getUserById(id);
        if (ret1.getCode() != ReturnNo.OK) {
            return ret1;
        }

        User user = ret1.getData();
        if (1 == user.getSign()){
            return new ReturnObject(ReturnNo.RESOURCE_FALSIFY);
        }

        ReturnObject returnObject;
        try {
            returnObject = ImgHelper.remoteSaveImg(multipartFile, 100000, davUsername, davPassword, baseUrl);

            //文件上传错误
            if (returnObject.getCode() != ReturnNo.OK) {
                logger.debug(returnObject.getErrmsg());
                return returnObject;
            }

            String oldFilename = user.getAvatar();
            user.setAvatar(returnObject.getData().toString());
            ReturnObject updateReturnObject = userDao.modifyUser(user);

            //数据库更新失败，需删除新增的图片
            if (updateReturnObject.getCode() != ReturnNo.OK) {
                ImgHelper.deleteRemoteImg(returnObject.getData().toString(), davUsername, davPassword, baseUrl);
                return updateReturnObject;
            }

            //数据库更新成功需删除旧图片，未设置则不删除
            if (oldFilename != null) {
                ImgHelper.deleteRemoteImg(oldFilename, davUsername, davPassword, baseUrl);
            }
        } catch (IOException e) {
            logger.debug("uploadImg: I/O Error:" + baseUrl);
            return new ReturnObject(ReturnNo.FILE_NO_WRITE_PERMISSION);
        }
        return returnObject;
    }


    /**
     * 查看任意用户的角色
     *
     * @param userId   用户id
     * @param page     页数
     * @param pageSize 每页大小
     * @return Object 角色返回视图
     * createdBy 王文凯 2021/11/26 11:44
     * @author 22920192204289 王文凯
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectRoles(Long userId, Long did, Integer page, Integer pageSize) {
        return userDao.selectRoles(userId, did, page, pageSize);
    }

    /**
     * 查看自己的角色
     *
     * @param page     页数
     * @param pageSize 每页大小
     * @return Object 角色返回视图
     * createdBy 王文凯 2021/11/26 11:44
     * @author 22920192204289 王文凯
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectSelfRoles(Long userId, Long did, Integer page, Integer pageSize) {
        return userDao.selectRoles(userId, did, page, pageSize);
    }

    /**
     * 查看自己的功能角色
     *
     * @param page     页数
     * @param pageSize 每页大小
     * @return Object 角色返回视图
     * createdBy 王文凯 2021/11/26 11:44
     * @author 22920192204289 王文凯
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectSelfBaseRoles(Long userId, Long did, Integer page, Integer pageSize) {
        return userDao.selectBaseRoles(userId, did, page, pageSize);
    }

    /**
     * 获得用户的功能角色
     *
     * @param did: 部门 id
     * @param id:  用户 id
     * @return Object
     * @author 22920192204320 张晖婧
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject findBaserolesByUserId(Long did, Long id, Integer page, Integer pageSize) {
        return userDao.selectBaseRoles(id, did, page, pageSize);
    }

    /**
     * 内部api-将某个用户的权限信息装载到Redis中
     *
     * @param userId: 用户 id
     * @return Object 装载的用户id
     * @author RenJie Zheng 22920192204334
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject loadUserPrivilege(Long userId, String jwt) {
        return userDao.loadUserPriv(userId, jwt);
    }


    /**
     * 赋予用户角色
     *
     * @param userid 用户id
     * @param roleid 角色id
     * @param did    departid
     * @return UserRoleRetVo
     * @author 张晖婧
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<VoObject> assignRole(Long creatorId, String creatorName, Long userid, Long roleid, Long did) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userid);
        userRole.setRoleId(roleid);
        Common.setPoCreatedFields(userRole, creatorId, creatorName);
        Common.setPoModifiedFields(userRole, creatorId, creatorName);

        ReturnObject returnObject = userDao.assignRole(userRole, did);

        return Common.getRetVo(returnObject, UserRoleSimpleRetVo.class);
    }
}
