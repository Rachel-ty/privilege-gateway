package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseSign;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.*;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.*;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.*;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.ModifyPwdVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.ResetPwdVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserVo;
import cn.edu.xmu.privilegegateway.annotation.util.*;
import cn.edu.xmu.privilegegateway.annotation.util.encript.AES;
import cn.edu.xmu.privilegegateway.annotation.util.encript.SHA256;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Ming Qiu
 * @date Created in 2020/11/1 11:48
 * Modified in 2020/11/8 0:57
 **/
@Repository
public class UserDao{

    @Autowired
    private UserPoMapper userPoMapper;

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);

    // 用户在Redis中的过期时间，而不是JWT的有效期
    @Value("${privilegeservice.user.expiretime}")
    private long timeout;


    @Autowired
    private UserRolePoMapper userRolePoMapper;

    @Autowired
    private UserGroupPoMapper userGroupPoMapper;

    @Autowired
    private UserProxyPoMapper userProxyPoMapper;

    @Autowired
    private UserPoMapper userMapper;

    @Autowired
    private RolePoMapper rolePoMapper;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private BaseCoder baseCoder;


    final static List<String> newUserSignFields = new ArrayList<>(Arrays.asList("userName", "password", "mobile", "email","name","idNumber",
            "passportNumber"));
    final static Collection<String> newUserCodeFields = new ArrayList<>(Arrays.asList("userName", "password", "mobile", "email","name","idNumber",
            "passportNumber"));
    //user表需要加密的全部字段
    final static Collection<String> userCodeFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile","idNumber","passportNumber"));
    //user表校验的所有字段
    final static List<String> userSignFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile","idNumber","passportNumber","state","departId","level"));

    final static List<String> userProxySignFields = new ArrayList<>(Arrays.asList("userId", "proxyUserId", "beginDate","expireDate"));
    final static Collection<String> userProxyCodeFields = new ArrayList<>();
    final static List<String> userRoleSignFields = new ArrayList<>(Arrays.asList("userId", "roleId"));
    final static Collection<String> userRoleCodeFields = new ArrayList<>();


//    @Autowired
//    private JavaMailSender mailSender;

    /**
     * 用户的redis key： u_id
     *
     */
    private final static String USERKEY = "u_%d";

    /**
     * 最终用户的redis key: up_id
     */
    private final static String USERPROXYKEY = "up_%d";
    /**
     * 用户ip的redis key: ip_id
     */
    private final static String IPKEY = "ip_%s";
    /**
     * 验证码的redis key: cp_id
     */
    private final static String CAPTCHAKEY = "cp_%s";

    /**
     * @author yue hao
     * @param id 用户ID
     * @return 用户的权限列表
     */

    public ReturnObject<List> findPrivsByUserId(Long id, Long did) {
        //getRoleIdByUserId已经进行签名校验
        User user = getUserById(id.longValue()).getData();
        if (user == null) {//判断是否是由于用户不存在造成的
            logger.error("findPrivsByUserId: 数据库不存在该用户 userid=" + id);
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        Long departId = user.getDepartId();
        if(departId != did) {
            logger.error("findPrivsByUserId: 店铺id不匹配 userid=" + id);
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        List<Long> roleIds = roleDao.getRoleIdByUserId(id);
        List<Privilege> privileges = new ArrayList<>();
        for(Long roleId: roleIds) {
            List<Privilege> rolePriv = roleDao.findPrivsByRoleId(roleId);
            privileges.addAll(rolePriv);
        }
        return new ReturnObject<>(privileges);
    }


    /**
     * 由用户名获得用户
     *
     * @param userName
     * @return
     */
    public ReturnObject<User> getUserByName(String userName) {
        UserPoExample example = new UserPoExample();
        UserPoExample.Criteria criteria = example.createCriteria();
        criteria.andUserNameEqualTo(userName);
        List<UserPo> users = null;
        try {
            users = userPoMapper.selectByExample(example);
        } catch (DataAccessException e) {
            StringBuilder message = new StringBuilder().append("getUserByName: ").append(e.getMessage());
            logger.error(message.toString());
        }

        if (null == users || users.isEmpty()) {
            return new ReturnObject<>();
        } else {
            User user = new User(users.get(0));
            if (!user.authetic()) {
                StringBuilder message = new StringBuilder().append("getUserByName: ").append("id= ")
                        .append(user.getId()).append(" username=").append(user.getUserName());
                logger.error(message.toString());
                return new ReturnObject<>(ReturnNo.RESOURCE_FALSIFY);
            } else {
                return new ReturnObject<>(user);
            }
        }
    }

    /**
     * @param userId 用户ID
     * @param IPAddr IP地址
     * @param date   登录时间
     * @return 是否成功更新
     */
    public Boolean setLoginIPAndPosition(Long userId, String IPAddr, LocalDateTime date) {
        UserPo userPo = new UserPo();
        userPo.setId(userId);
        userPo.setLastLoginIp(IPAddr);
        userPo.setLastLoginTime(date);
        if (userPoMapper.updateByPrimaryKeySelective(userPo) == 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 取消用户角色
     * @param userid 用户id
     * @param roleid 角色id
     * @return ReturnObject<VoObject>
     * @author Xianwei Wang
     * */
    public ReturnObject<VoObject> revokeRole(Long userid, Long roleid){
        UserRolePoExample userRolePoExample = new UserRolePoExample();
        UserRolePoExample.Criteria criteria = userRolePoExample.createCriteria();
        criteria.andUserIdEqualTo(userid);
        criteria.andRoleIdEqualTo(roleid);

        User user = getUserById(userid.longValue()).getData();
        RolePo rolePo = rolePoMapper.selectByPrimaryKey(roleid);

        //用户id或角色id不存在
        if (user == null || rolePo == null) {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        try {
            int state = userRolePoMapper.deleteByExample(userRolePoExample);
            if (state == 0){
                logger.warn("revokeRole: 未找到该用户角色");
                return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
            }


        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }

        //清除缓存
        clearUserPrivCache(userid);

        return new ReturnObject<>();
    }

    /**
     * 赋予用户角色
     * @param createid 创建者id
     * @param userid 用户id
     * @param roleid 角色id
     * @return ReturnObject<VoObject>
     * @author Xianwei Wang
     * */
    public ReturnObject<VoObject> assignRole(Long createid, Long userid, Long roleid){
        UserRolePo userRolePo = new UserRolePo();
        userRolePo.setUserId(userid);
        userRolePo.setRoleId(roleid);

        User user = getUserById(userid.longValue()).getData();
        User create = getUserById(createid.longValue()).getData();
        RolePo rolePo = rolePoMapper.selectByPrimaryKey(roleid);

        //用户id或角色id不存在
        if (user == null || create == null || rolePo == null) {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }

        userRolePo.setCreatorId(createid);
        userRolePo.setGmtCreate(LocalDateTime.now());

        UserRole userRole = new UserRole(userRolePo, user, new Role(rolePo), create);
        userRolePo.setSignature(userRole.getCacuSignature());

        //查询该用户是否已经拥有该角色
        UserRolePoExample example = new UserRolePoExample();
        UserRolePoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userid);
        criteria.andRoleIdEqualTo(roleid);

        //若未拥有，则插入数据
        try {
            List<UserRolePo> userRolePoList = userRolePoMapper.selectByExample(example);
            if (userRolePoList.isEmpty()){
                userRolePoMapper.insert(userRolePo);
            } else {
                logger.warn("assignRole: 该用户已拥有该角色 userid=" + userid + "roleid=" + roleid);
                return new ReturnObject<>(ReturnNo.OK);
            }
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
        //清除缓存
        clearUserPrivCache(userid);

        return new ReturnObject(new UserRole(userRolePo, user, new Role(rolePo), create));

    }

    /**
     * 使用用户id，清空该用户和被代理对象的redis缓存
     * @param userid 用户id
     * @author Xianwei Wang
     */
    private void clearUserPrivCache(Long userid){
        String key = String.format(USERKEY , userid);
        redisTemplate.delete(key);

        UserProxyPoExample example = new UserProxyPoExample();
        UserProxyPoExample.Criteria criteria = example.createCriteria();
        criteria.andProxyUserIdEqualTo(userid);
        List<UserProxyPo> userProxyPoList = userProxyPoMapper.selectByExample(example);

        LocalDateTime now = LocalDateTime.now();

        for (UserProxyPo po:
             userProxyPoList) {
            StringBuilder signature = Common.concatString("-", po.getUserId().toString(),
                    po.getProxyUserId().toString(), po.getBeginDate().toString(), po.getEndDate().toString(), po.getValid().toString());
            String newSignature = SHA256.getSHA256(signature.toString());
            UserProxyPo newPo = null;

            if (newSignature.equals(po.getSignature())) {
                if (now.isBefore(po.getEndDate()) && now.isAfter(po.getBeginDate())) {
                    //在有效期内
                    String proxyKey = String.format(USERPROXYKEY, po.getUserId());
                    redisTemplate.delete(proxyKey);
                    logger.debug("clearUserPrivCache: userAId = " + po.getUserId() + " userBId = " + po.getProxyUserId());
                } else {
                    //代理过期了，但标志位依然是有效
                    newPo = newPo == null ? new UserProxyPo() : newPo;
                    newPo.setValid((byte) 0);
                    signature = Common.concatString("-", po.getUserId().toString(),
                            po.getProxyUserId().toString(), po.getBeginDate().toString(), po.getEndDate().toString(), newPo.getValid().toString());
                    newSignature = SHA256.getSHA256(signature.toString());
                    newPo.setSignature(newSignature);
                }
            } else {
                logger.error("clearUserPrivCache: Wrong Signature(auth_user_proxy): id =" + po.getId());
            }

            if (null != newPo) {
                logger.debug("clearUserPrivCache: writing back.. po =" + newPo);
                userProxyPoMapper.updateByPrimaryKeySelective(newPo);
            }

        }
    }

    /**
     * 获取用户的角色信息
     * @param id 用户id
     * @return UserRole列表
     * @author Xianwei Wang
     * */
    public ReturnObject<List> getUserRoles(Long id){
        UserRolePoExample example = new UserRolePoExample();
        UserRolePoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(id);
        List<UserRolePo> userRolePoList = userRolePoMapper.selectByExample(example);
        logger.info("getUserRoles: userId = "+ id + "roleNum = "+ userRolePoList.size());

        List<UserRole> retUserRoleList = new ArrayList<>(userRolePoList.size());

        if (retUserRoleList.isEmpty()) {
            User user = getUserById(id.longValue()).getData();
            if (user == null) {
                logger.error("getUserRoles: 数据库不存在该用户 userid=" + id);
                return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
        }

        for (UserRolePo po : userRolePoList) {
            User user = getUserById(po.getUserId().longValue()).getData();
            User creator = getUserById(po.getCreatorId().longValue()).getData();
            RolePo rolePo = rolePoMapper.selectByPrimaryKey(po.getRoleId());
            if (user == null) {
                return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            if (creator == null) {
                logger.error("getUserRoles: 数据库不存在该资源 userid=" + po.getCreatorId());
            }
            if (rolePo == null) {
                logger.error("getUserRoles: 数据库不存在该资源:rolePo id=" + po.getRoleId());
                continue;
            }

            Role role = new Role(rolePo);
            UserRole userRole = new UserRole(po, user, role, creator);

            //校验签名
            if (userRole.authetic()){
                retUserRoleList.add(userRole);
                logger.info("getRoleIdByUserId: userId = " + po.getUserId() + " roleId = " + po.getRoleId());
            } else {
                logger.error("getUserRoles: Wrong Signature(auth_user_role): id =" + po.getId());
            }
        }
        return new ReturnObject<>(retUserRoleList);
    }


    /**
     * @description 检查用户的departid是否与路径上的一致
     * @param userid 用户id
     * @param departid 路径上的departid
     * @return boolean
     * @author Xianwei Wang
     * created at 11/20/20 1:48 PM
     */
    public boolean checkUserDid(Long userid, Long departid) {
        UserPo userPo = userMapper.selectByPrimaryKey(userid);
        if (userPo == null) {
            return false;
        }
        if (userPo.getDepartId() != departid) {
            return false;
        }
        return true;
    }

    /**
     * @description 检查角色的departid是否与路径上的一致
     * @param roleid 角色id
     * @param departid 路径上的departid
     * @return boolean
     * @author Xianwei Wang
     * created at 11/20/20 1:51 PM
     */
    public boolean checkRoleDid(Long roleid, Long departid) {
        RolePo rolePo = rolePoMapper.selectByPrimaryKey(roleid);
        if (rolePo == null) {
            return false;
        }
        if (rolePo.getDepartId() != departid) {
            return false;
        }
        return true;
    }


    /**
     * 计算User自己的权限，load到Redis
     *
     * @param id userID
     * @return void
     * <p>
     * createdBy: Ming Qiu 2020-11-02 11:44
     * modifiedBy: Ming Qiu 2020-11-03 12:31
     * 将获取用户Roleid的代码独立, 增加redis过期时间
     * modifiedBy Ming Qiu 2020-11-07 8:00
     * 集合里强制加“0”
     * modified by Ming Qiu 2021-11-21 19:34
     *   将redisTemplate 替换成redisUtil
     */
    private void loadSingleUserPriv(Long id) {
        List<Long> roleIds = roleDao.getRoleIdByUserId(id);
        String key = String.format(USERKEY, id);
        Set<String> roleKeys = new HashSet<>(roleIds.size());
        for (Long roleId : roleIds) {
            String roleKey = String.format(RoleDao.ROLEKEY, roleId);
            roleKeys.add(roleKey);
            if (!redisUtil.hasKey(roleKey)) {
                roleDao.loadRolePriv(roleId);
            }
            redisUtil.unionAndStoreSet(roleKeys, key);
        }
        redisUtil.addSet(key, 0);
        long randTimeout = Common.addRandomTime(timeout);
        redisUtil.expire(key, randTimeout, TimeUnit.SECONDS);
    }




    /**
     * 计算User的权限（包括代理用户的权限，只计算直接代理用户），load到Redis
     *
     * @param id userID
     * @return void
     * createdBy Ming Qiu 2020/11/1 11:48
     * modifiedBy Ming Qiu 2020/11/3 14:37
     * modified by Ming Qiu 2021-11-21 19:34
     *   将redisTemplate 替换成redisUtil
     */
    public void loadUserPriv(Long id, String jwt) {

        String key = String.format(USERKEY, id);
        String aKey = String.format(USERPROXYKEY,  id);

        List<Long> proxyIds = this.getProxyIdsByUserId(id);
        List<String> proxyUserKey = new ArrayList<>(proxyIds.size());
        for (Long proxyId : proxyIds) {
            if (!redisUtil.hasKey(String.format(USERKEY, proxyId))) {
                logger.debug("loadUserPriv: loading proxy user. proxId = " + proxyId);
                loadSingleUserPriv(proxyId);
            }
            proxyUserKey.add(String.format(USERKEY, proxyId));
        }
        if (!redisUtil.hasKey(key)) {
            logger.debug("loadUserPriv: loading user. id = " + id);
            loadSingleUserPriv(id);
        }
        redisUtil.unionAndStoreSet(key, proxyUserKey, aKey);
        redisUtil.addSet(aKey, jwt);
        long randTimeout = Common.addRandomTime(timeout);
        redisUtil.expire(aKey, randTimeout, TimeUnit.SECONDS);
    }

    /**
     * 获得代理的用户id列表
     *
     * @param id 用户id
     * @return 被代理的用户id
     * createdBy Ming Qiu 14:37
     */
    private List<Long> getProxyIdsByUserId(Long id) {
        UserProxyPoExample example = new UserProxyPoExample();
        //查询当前所有有效的被代理用户
        UserProxyPoExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(id);
        criteria.andValidEqualTo((byte) 1);
        List<UserProxyPo> userProxyPos = userProxyPoMapper.selectByExample(example);
        List<Long> retIds = new ArrayList<>(userProxyPos.size());
        LocalDateTime now = LocalDateTime.now();
        for (UserProxyPo po : userProxyPos) {
            StringBuilder signature = Common.concatString("-", po.getUserId().toString(),
                    po.getProxyUserId().toString(), po.getBeginDate().toString(), po.getEndDate().toString(), po.getValid().toString());
            String newSignature = SHA256.getSHA256(signature.toString());
            UserProxyPo newPo = null;

            if (newSignature.equals(po.getSignature())) {
                if (now.isBefore(po.getEndDate()) && now.isAfter(po.getBeginDate())) {
                    //在有效期内
                    retIds.add(po.getProxyUserId());
                    logger.debug("getProxyIdsByUserId: userAId = " + po.getUserId() + " userBId = " + po.getProxyUserId());
                } else {
                    //代理过期了，但标志位依然是有效
                    newPo = newPo == null ? new UserProxyPo() : newPo;
                    newPo.setValid((byte) 0);
                    signature = Common.concatString("-", po.getUserId().toString(),
                            po.getProxyUserId().toString(), po.getBeginDate().toString(), po.getEndDate().toString(), newPo.getValid().toString());
                    newSignature = SHA256.getSHA256(signature.toString());
                    newPo.setSignature(newSignature);
                }
            } else {
                logger.error("getProxyIdsByUserId: Wrong Signature(auth_user_proxy): id =" + po.getId());
            }

            if (null != newPo) {
                logger.debug("getProxyIdsByUserId: writing back.. po =" + newPo);
                userProxyPoMapper.updateByPrimaryKeySelective(newPo);
            }
        }
        return retIds;
    }

    public void initialize() throws Exception {
        //初始化user
        UserPoExample example = new UserPoExample();
        UserPoExample.Criteria criteria = example.createCriteria();
        criteria.andSignatureIsNull();

        List<UserPo> userPos = userMapper.selectByExample(example);

        for (UserPo po : userPos) {
            UserPo newUserPo = (UserPo)baseCoder.code_sign(po,UserPo.class,userCodeFields,userSignFields,"signature");
            userMapper.updateByPrimaryKeySelective(newUserPo);
        }

        //初始化UserProxy
        UserProxyPoExample example1 = new UserProxyPoExample();
        UserProxyPoExample.Criteria criteria1 = example1.createCriteria();
        criteria1.andSignatureIsNull();
        List<UserProxyPo> userProxyPos = userProxyPoMapper.selectByExample(example1);

        for (UserProxyPo po : userProxyPos) {
            UserProxyPo newUserProxyPo = (UserProxyPo) baseCoder.code_sign(po,UserProxyPo.class,userProxyCodeFields,userProxySignFields,"signature");
            userProxyPoMapper.updateByPrimaryKeySelective(newUserProxyPo);
        }

        //初始化UserRole
        UserRolePoExample example3 = new UserRolePoExample();
        UserRolePoExample.Criteria criteria3 = example3.createCriteria();
        criteria3.andSignatureIsNull();
        List<UserRolePo> userRolePoList = userRolePoMapper.selectByExample(example3);
        for (UserRolePo po : userRolePoList) {
            UserRolePo newUserRolePo = (UserRolePo) baseCoder.code_sign(po,UserRole.class,userRoleCodeFields,userRoleSignFields,"signature");
            userRolePoMapper.updateByPrimaryKeySelective(newUserRolePo);
        }

    }

    /**
     * 获得用户
     *
     * @param id userID
     * @return User
     * createdBy 3218 2020/11/4 15:48
     * modifiedBy 3218 2020/11/4 15:48
     */

    public ReturnObject<User> getUserById(Long id) {
        UserPo userPo = userMapper.selectByPrimaryKey(id);
        if (userPo == null) {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        User user = new User(userPo);
        if (!user.authetic()) {
            StringBuilder message = new StringBuilder().append("getUserById: ").append(ReturnNo.RESOURCE_FALSIFY.getMessage()).append(" id = ")
                    .append(user.getId()).append(" username =").append(user.getUserName());
            logger.error(message.toString());
            return new ReturnObject<>(ReturnNo.RESOURCE_FALSIFY);
        }
        return new ReturnObject<>(user);
    }


    /**
     * 更新用户图片
     *
     * @param user
     * @return User
     * createdBy 3218 2020/11/4 15:55
     * modifiedBy 3218 2020/11/4 15:55
     */
    public ReturnObject updateUserAvatar(User user) {
        ReturnObject returnObject = new ReturnObject();
        UserPo newUserPo = new UserPo();
        newUserPo.setId(user.getId());
        newUserPo.setAvatar(user.getAvatar());
        int ret = userMapper.updateByPrimaryKeySelective(newUserPo);
        if (ret == 0) {
            logger.debug("updateUserAvatar: update fail. user id: " + user.getId());
            returnObject = new ReturnObject(ReturnNo.FIELD_NOTVALID);
        } else {
            logger.debug("updateUserAvatar: update user success : " + user.toString());
            returnObject = new ReturnObject();
        }
        return returnObject;
    }

    /**
     * ID获取用户信息
     * @author XQChen
     * @param Id
     * @return 用户
     */
    public UserPo findUserById(Long Id) {
        UserPoExample example = new UserPoExample();
        UserPoExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(Id);

        logger.debug("findUserById: Id =" + Id);
        UserPo userPo = userPoMapper.selectByPrimaryKey(Id);

        return userPo;
    }

    /**
     * ID获取用户信息
     * @author XQChen
     * @param id
     * @param did
     * @return 用户
     */
    public UserPo findUserByIdAndDid(Long id, Long did) {
        UserPoExample example = new UserPoExample();
        UserPoExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(id);
        criteria.andDepartIdEqualTo(did);

        logger.debug("findUserByIdAndDid: Id =" + id + " did = " + did);
        UserPo userPo = userPoMapper.selectByPrimaryKey(id);

        return userPo;
    }

    /**
     * 获取所有用户信息
     * @author XQChen
     * @return List<UserPo> 用户列表
     */
    public PageInfo<UserPo> findAllUsers(String userNameAES, String mobileAES, Long did) {
        UserPoExample example = new UserPoExample();
        UserPoExample.Criteria criteria = example.createCriteria();
        criteria.andDepartIdEqualTo(did);
        if(!userNameAES.isBlank())
            criteria.andUserNameEqualTo(userNameAES);
        if(!mobileAES.isBlank())
            criteria.andMobileEqualTo(mobileAES);

        List<UserPo> users = userPoMapper.selectByExample(example);

        logger.debug("findUserById: retUsers = "+users);

        return new PageInfo<>(users);
    }

    /* auth009 */
    /**
     * 获得解密后的Po并校验签名
     *
     * @param id userID
     * @return UserPo
     * createdBy 蒋欣雨 2021/12/1
     */

    public ReturnObject getUserPoById(Long id) {
        // 查询密码等资料以计算新签名
        try
        {
            UserPo userPo = userMapper.selectByPrimaryKey(id);
            // 不修改已被逻辑废弃的账户
            if (userPo == null || (userPo.getState() != null && User.State.getTypeByCode(userPo.getState().intValue()) == User.State.DELETE)) {
                logger.info("用户不存在或已被删除：id = " + id);
                return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
            }

            userPo=(UserPo) baseCoder.decode_check(userPo,UserPo.class,userCodeFields,userSignFields,"signature");
            //签名校验失败
            if (userPo.getSignature()==null)
            {
                return new ReturnObject<>(ReturnNo.RESOURCE_FALSIFY);
            }

            return new ReturnObject<>(userPo);
        }
        catch (Exception e) {
            // 其他 Exception 即属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
    }

    /**
     * 根据 id 修改用户信息
     *
     * @param userVo 传入的 User 对象
     * @return 返回对象 ReturnObj
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    public ReturnObject<Object> modifyUserByVo(Long did,Long id, UserVo userVo,Long loginUser,String loginName) {
        if (!checkUserDid(id, did)&&did != Long.valueOf(0)) {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE);
        }

        // 更新数据库
        ReturnObject<Object> retObj=getUserPoById(id);
        if(retObj.getCode()!=ReturnNo.OK)
            return retObj;
        // 查询密码等资料以计算新签名
        UserPo userPo =(UserPo) retObj.getData();
        userPo.setName(userVo.getName());
        userPo.setAvatar(userVo.getAvatar());
        userPo.setIdNumber(userVo.getIdNumber());
        userPo.setPassportNumber(userVo.getPassportNumber());
        userPo.setLevel(userVo.getLevel());
        Common.setPoModifiedFields(userPo,loginUser,loginName);
        userPo = (UserPo) baseCoder.code_sign(userPo, UserPo.class, userCodeFields, userSignFields, "signature");

        int ret;
        try{
            //更新修改
            ret = userMapper.updateByPrimaryKeySelective(userPo);
            } catch (DataAccessException e) {
                    // 其他情况属未知错误
                    logger.error("数据库错误：" + e.getMessage());
                    retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,
                            String.format("发生了严重的数据库错误：%s", e.getMessage()));
                return retObj;
        } catch (Exception e) {
            // 其他 Exception 即属未知错误
            logger.error("严重错误：" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
        // 检查更新有否成功
        if (ret == 0) {
            logger.info("用户不存在或已被删除：id = " + id);
            retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        } else {
            logger.info("用户 id = " + id + " 的资料已更新");
            retObj = new ReturnObject<>();
        }
        return retObj;
    }

    /**
     * (物理) 删除用户
     *
     * @param id 用户 id
     * @return 返回对象 ReturnObj
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     */
    public ReturnObject<Object> physicallyDeleteUser(Long id) {
        ReturnObject<Object> retObj;
        int ret = userMapper.deleteByPrimaryKey(id);
        if (ret == 0) {
            logger.info("用户不存在或已被删除：id = " + id);
            retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        } else {
            logger.info("用户 id = " + id + " 已被永久删除");
            retObj = new ReturnObject<>();
        }
        return retObj;
    }

    /**
     * 创建可改变目标用户状态的 Po
     *
     * @param id    用户 id
     * @param state 用户目标状态
     * @return UserPo 对象
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     */
    private UserPo createUserStateModPo(Long id, User.State state) {
        // 查询密码等资料以计算新签名
        UserPo orig = userMapper.selectByPrimaryKey(id);
        // 不修改已被逻辑废弃的账户的状态
        if (orig == null || (orig.getState() != null && User.State.getTypeByCode(orig.getState().intValue()) == User.State.DELETE)) {
            return null;
        }

        // 构造 User 对象以计算签名
        User user = new User(orig);
        user.setState(state);
        // 构造一个全为 null 的 vo 因为其他字段都不用更新
        UserVo vo = new UserVo();

        return user.createUpdatePo(vo);
    }



    /**
     * 改变用户状态
     *
     * @param id    用户 id
     * @param state 目标状态
     * @return 返回对象 ReturnObj
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:30
     * Modified by 19720182203919 李涵 at 2020/11/5 10:42
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    public ReturnObject<Object> changeUserState(Long did,Long id,Long loginUser,String loginName, User.State state) {
        if (!checkUserDid(id, did)&&did != Long.valueOf(0)) {
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE);
        }

        ReturnObject<Object> retObj=getUserPoById(id);
        if(retObj.getCode()!=ReturnNo.OK)
            return retObj;

        // 查询密码等资料以计算新签名
        UserPo userPo =(UserPo) retObj.getData();
        userPo.setState(state.getCode().byteValue());
        Common.setPoModifiedFields(userPo,loginUser,loginName);

        userPo= (UserPo) baseCoder.code_sign(userPo, UserPo.class, userCodeFields, userSignFields, "signature");
        int ret;
        try {
            ret = userMapper.updateByPrimaryKeySelective(userPo);
            if (ret == 0) {
                logger.info("用户不存在或已被删除：id = " + id);
                retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
            } else {
                logger.info("用户 id = " + id + " 的状态修改为 " + state.getDescription());
                retObj = new ReturnObject<>();
            }
        } catch (DataAccessException e) {
            // 数据库错误
            logger.error("数据库错误：" + e.getMessage());
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的数据库错误：%s", e.getMessage()));
        } catch (Exception e) {
            // 属未知错误
            logger.error("严重错误：" + e.getMessage());
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,
                    String.format("发生了严重的未知错误：%s", e.getMessage()));
        }
        return retObj;
    }

    /* auth009 ends */

        /* auth002 begin*/

    /**
     * auth002: 用户重置密码
     * @param vo 重置密码对象
     * @param ip 请求ip地址
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    public ReturnObject<Object> resetPassword(ResetPwdVo vo, String ip) {

        //防止重复请求验证码
        String key = String.format(IPKEY, ip);
        if (redisUtil.hasKey(key))
            return new ReturnObject<>(ReturnNo.AUTH_USER_FORBIDDEN);
        else {
            //1 min中内不能重复请求
            redisUtil.set(key, ip, 60L);
        }

        //验证邮箱、手机号

        UserPoExample userPoExample1 = new UserPoExample();
        List<UserPo> userPo1 = null;
        Collection<String> voCodeFields = new ArrayList<>(Arrays.asList("name"));
        ResetPwdVo vo_coded= (ResetPwdVo) baseCoder.code_sign(vo, ResetPwdVo.class, voCodeFields,null, "signature");

        try {
            UserPoExample.Criteria criteria_email = userPoExample1.createCriteria();
            criteria_email.andEmailEqualTo(vo_coded.getName());
            UserPoExample.Criteria criteria_phone = userPoExample1.createCriteria();
            criteria_phone.andMobileEqualTo(vo_coded.getName());
            UserPoExample.Criteria criteria_username = userPoExample1.createCriteria();
            criteria_username.andUserNameEqualTo(vo.getName());
            userPoExample1.or(criteria_phone);
            userPoExample1.or(criteria_username);
            userPo1 = userMapper.selectByExample(userPoExample1);
            if (userPo1.isEmpty()) {
             return new ReturnObject<>(ReturnNo.EMAIL_WRONG);
            }

        } catch (Exception e) {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }



        //随机生成验证码
        String captcha = RandomCaptcha.getRandomString(6);
        while (redisUtil.hasKey(captcha))
            captcha = RandomCaptcha.getRandomString(6);

        String id = userPo1.get(0).getId().toString();
        key =  String.format(CAPTCHAKEY, captcha);
        redisUtil.set(key, id, 5*60L);


//        //发送邮件(请在配置文件application.properties填写密钥)
//        SimpleMailMessage msg = new SimpleMailMessage();
//        msg.setSubject("【oomall】密码重置通知");
//        msg.setSentDate(new Date());
//        msg.setText("您的验证码是：" + captcha + "，5分钟内有效。");
//        msg.setFrom("925882085@qq.com");
//        msg.setTo(vo.getEmail());
//        try {
//            mailSender.send(msg);
//        } catch (MailException e) {
//            return new ReturnObject<>(ReturnNo.FIELD_NOTVALID);
//        }

        return new ReturnObject<>(captcha);
    }

    /**
     * auth002: 用户修改密码
     * @param modifyPwdVo 修改密码对象
     * @return Object
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    public ReturnObject<Object> modifyPassword(ModifyPwdVo modifyPwdVo) {

        //防止重复请求验证码
        String key = String.format(CAPTCHAKEY, modifyPwdVo.getCaptcha());

        //通过验证码取出id
        if (!redisUtil.hasKey(key))
            return new ReturnObject<>(ReturnNo.AUTH_INVALID_ACCOUNT);
        Long id = (Long) redisUtil.get(key);

        ReturnObject<Object> retObj=getUserPoById(id);
        if(retObj.getCode()!=ReturnNo.OK)
            return retObj;
        // 查询密码等资料以计算新签名
        UserPo userPo =(UserPo) retObj.getData();

        //新密码与原密码相同
        if (userPo.getPassword().equals(modifyPwdVo.getNewPassword()))
            return new ReturnObject<>(ReturnNo.PASSWORD_SAME);
        userPo.setPassword(modifyPwdVo.getNewPassword());
        userPo = (UserPo) baseCoder.code_sign(userPo, UserPo.class, userCodeFields, userSignFields, "signature");
        //更新数据库
        try {
            userMapper.updateByPrimaryKeySelective(userPo);
        } catch (Exception e) {
            e.printStackTrace();
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
        return new ReturnObject<>(ReturnNo.OK);
    }

    /* auth002 end*/


    /**
     * 清除缓存中的与role关联的user
     *
     * @param id 角色id
     * createdBy 王琛 24320182203277
     */
    public void clearUserByRoleId(Long id){
        UserRolePoExample example = new UserRolePoExample();
        UserRolePoExample.Criteria criteria = example.createCriteria();
        criteria.andRoleIdEqualTo(id);

        List<UserRolePo> userrolePos = userRolePoMapper.selectByExample(example);
        Long uid;
        for(UserRolePo e:userrolePos){
            uid = e.getUserId();
            clearUserPrivCache(uid);
        }
    }
     /**
     * 创建user
     *
     * createdBy Li Zihan 243201822032227
     * Created by 22920192204219 蒋欣雨 at 2021/11/29
     */
    public ReturnObject addUser(NewUserPo po,Long loginUser,String loginName) {
        ReturnObject returnObject = null;
        UserPo userPo =(UserPo) Common.cloneVo(po,UserPo.class);
        Common.setPoCreatedFields(userPo,loginUser,loginName);
        userPo = (UserPo) baseCoder.code_sign(userPo, UserPo.class, null,userSignFields, "signature");

        try {
            returnObject = new ReturnObject<>(userPoMapper.insert(userPo));
            logger.debug("success insert User: " + userPo.getId());
        } catch (DataAccessException e) {
            if (Objects.requireNonNull(e.getMessage()).contains("auth_user.user_name_uindex")) {
                //若有重复名则修改失败
                logger.debug("insertUser: have same user name = " + userPo.getName());
                returnObject = new ReturnObject<>(ReturnNo.ROLE_EXIST, String.format("用户名重复：" + userPo.getName()));
            } else {
                logger.debug("sql exception : " + e.getMessage());
                returnObject = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
            }
        } catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            returnObject = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        return returnObject;
    }

    /**
     * 功能描述: 修改用户depart
     * @Param: userId departId
     * @Return:
     * @Author: Yifei Wang
     * @Date: 2020/12/8 11:35
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    public InternalReturnObject changeUserDepart(Long userId, Long departId,Long loginUser,String loginName) {

        try {
            UserPo userPo = userMapper.selectByPrimaryKey(userId);
            if(userPo.getDepartId()!=-1)
            {
                return new InternalReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE.getCode(),ReturnNo.RESOURCE_ID_OUTSCOPE.getMessage());
            }
            userPo.setDepartId(departId);
            Common.setPoModifiedFields(userPo,loginUser,loginName);
            userPo = (UserPo) baseCoder.code_sign(userPo, UserPo.class,null, userSignFields, "signature");

            logger.debug("Update User: " + userId);
            int ret = userPoMapper.updateByPrimaryKeySelective(userPo);
            if (ret == 0) {
                return new InternalReturnObject<>(ReturnNo.FIELD_NOTVALID);
            }
            logger.debug("Success Update User: " + userId);
            return new InternalReturnObject<>(ReturnNo.OK);
        } catch (Exception e) {
            logger.error("exception : " + e.getMessage());
            return new InternalReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }



    /**
     * 获取用户状态
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    public ReturnObject getUserState(){
        List<Map<String, Object>> stateList = new ArrayList<>();
        for (UserBo.State states : UserBo.State.values()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("code", states.getCode());
            temp.put("name", states.getDescription());
            stateList.add(temp);
        }
        return new ReturnObject<>(stateList);
    }

    /**
     * 修改用户信息
     * @param userBo
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    public ReturnObject modifyUser(UserBo userBo){
        UserPo userPo = (UserPo) baseCoder.code_sign(userBo,UserPo.class,userCodeFields,userSignFields,"signature");
        try {
            userPoMapper.updateByPrimaryKeySelective(userPo);
            return new ReturnObject();
        }catch (DuplicateKeyException e){
            String info=e.getMessage();
            if(info.contains("user_name_uindex")){
                return new ReturnObject(ReturnNo.USER_NAME_REGISTERED);
            }
            else if(info.contains("email_uindex")){
                return new ReturnObject(ReturnNo.EMAIL_REGISTERED);
            }
            else{
                return new ReturnObject(ReturnNo.MOBILE_REGISTERED);
            }
        }
        catch (Exception e){
            logger.error(e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 获取所有用户
     * @param did
     * @param userName
     * @param mobile
     * @param email
     * @param page
     * @param pageSize
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    public ReturnObject selectAllUsers(Long did, String userName, String mobile, String email, Integer page, Integer pageSize){
        UserPoExample example = new UserPoExample();
        UserPoExample.Criteria criteria= example.createCriteria();
        PageHelper.startPage(page,pageSize);
        List<UserPo> userPos = new ArrayList<>();
        UserBo userBo = new UserBo();
        userBo.setDepartId(did);
        userBo.setUserName(userName);
        userBo.setMobile(mobile);
        userBo.setEmail(email);
        UserBo encryptedUserBo = (UserBo) baseCoder.code_sign(userBo,UserBo.class,userCodeFields,userSignFields,"signature");
        try {
            criteria.andDepartIdEqualTo(did);
            if (userName!=null){
                criteria.andUserNameEqualTo(encryptedUserBo.getUserName());
            }
            if (mobile!=null){
                criteria.andMobileEqualTo(encryptedUserBo.getMobile());
            }
            if (email!=null){
                criteria.andEmailEqualTo(encryptedUserBo.getEmail());
            }
            userPos = userPoMapper.selectByExample(example);
            // TODO:验签
            for(UserPo userPo: userPos){
                if(null==baseCoder.decode_check(userPo,NewUserPo.class,userCodeFields,userSignFields,"signature")){
                    logger.error(ReturnNo.RESOURCE_FALSIFY.getMessage());
                    return new ReturnObject(ReturnNo.RESOURCE_FALSIFY);
                }
            }
            return new ReturnObject(userPos);
        }catch (DataAccessException e){
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }

    public ReturnObject updateUserAvatar(UserBo userBo) {
        ReturnObject returnObject = new ReturnObject();
        UserPo newUserPo = new UserPo();
        newUserPo.setId(userBo.getId());
        newUserPo.setAvatar(userBo.getAvatar());
        int ret = userMapper.updateByPrimaryKeySelective(newUserPo);
        if (ret == 0) {
            logger.debug("updateUserAvatar: update fail. user id: " + userBo.getId());
            returnObject = new ReturnObject(ReturnNo.FIELD_NOTVALID);
        } else {
            logger.debug("updateUserAvatar: update user success : " + userBo.toString());
            returnObject = new ReturnObject();
        }
        return returnObject;
    }
}

