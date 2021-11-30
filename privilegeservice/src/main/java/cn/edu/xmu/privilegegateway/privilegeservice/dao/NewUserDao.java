package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.NewUserPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.UserPoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.NewUserBo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.UserBo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.NewUserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.NewUserPoExample;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.NewUserVo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.encript.AES;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.annotation.util.bloom.BloomFilter;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.User;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 新用户Dao
 * @author LiangJi@3229
 * @date 2020/11/10 18:41
 */
@Repository
public class NewUserDao implements InitializingBean {
    private  static  final Logger logger = LoggerFactory.getLogger(NewUserDao.class);
    @Autowired
    NewUserPoMapper newUserPoMapper;
    @Autowired
    UserPoMapper userPoMapper;
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    BloomFilter<String> stringBloomFilter;

    final String EMAILFILTER="NewUserEmailBloomFilter";
    final String MOBILEFILTER="NewUserMobileBloomFilter";
    final String NAMEFILTER="NewUserNameBloomFilter";

    @Value("${privilegeservice.bloomfilter.new-user-email.error-rate}")
    private double emailError = 0.01;
    @Value("${privilegeservice.bloomfilter.new-user-email.capacity}")
    private int emailCapacity = 10000;

    @Value("${privilegeservice.bloomfilter.new-user-name.error-rate}")
    private double nameError = 0.01;
    @Value("${privilegeservice.bloomfilter.new-user-name.capacity}")
    private int nameCapacity = 10000;

    @Value("${privilegeservice.bloomfilter.new-user-mobile.error-rate}")
    private double mobileError = 0.01;
    @Value("${privilegeservice.bloomfilter.new-user-mobile.capacity}")
    private int mobileCapacity = 10000;

    @Autowired
    private BaseCoder baseCoder;
    final static List<String> signFields = new ArrayList<>(Arrays.asList("userName", "password", "mobile", "email","name","idNumber",
            "passportNumber"));
    final static Collection<String> codeFields = new ArrayList<>(Arrays.asList("userName", "password", "mobile", "email","name","idNumber",
            "passportNumber"));


    /**
     * 初始化布隆过滤器
     * @throws Exception
     * createdBy: LiangJi3229 2020-11-10 18:41
     * modifiedBy: Ming Qiu 2021-11-21 05:42
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        if (! stringBloomFilter.checkFilter(EMAILFILTER)) {
            stringBloomFilter.newFilter(EMAILFILTER, emailError, emailCapacity);
        }

        if (! stringBloomFilter.checkFilter(NAMEFILTER)) {
            stringBloomFilter.newFilter(NAMEFILTER, nameError, nameCapacity);
        }

        if (! stringBloomFilter.checkFilter(MOBILEFILTER)) {
            stringBloomFilter.newFilter(MOBILEFILTER, mobileError, mobileCapacity);
        }

    }

    /**
     *
     * @param po
     * @return ReturnObject 错误返回对象
     * createdBy: LiangJi3229 2020-11-10 18:41
     * modifiedBy: Ming Qiu 2021-11-21 06:11
     */
    public ReturnObject checkBloomFilter(NewUserPo po){
        if(stringBloomFilter.checkValue(EMAILFILTER, po.getEmail())){
            return new ReturnObject(ReturnNo.EMAIL_REGISTERED);
        }
        if(stringBloomFilter.checkValue(MOBILEFILTER, po.getMobile())){
            return new ReturnObject(ReturnNo.MOBILE_REGISTERED);
        }
        if(stringBloomFilter.checkValue(NAMEFILTER, po.getUserName())){
            return new ReturnObject(ReturnNo.USER_NAME_REGISTERED);
        }
        return null;

    }


    /**
     * 由vo创建newUser检查重复后插入
     * @param newUserBo
     * @return ReturnObject
     * createdBy: LiangJi3229 2020-11-10 18:41
     * modifiedBy: Ming Qiu 2021-11-21 06:11
     * modifiedBy: BIngShuai Liu 2021-11-29 19:58
     */
    public ReturnObject createNewUserByBo(NewUserBo newUserBo){
        //logger.debug(String.valueOf(bloomFilter.includeByBloomFilter("mobileBloomFilter","FAED5EEF1C8562B02110BCA3F9165CBE")));
        //by default,email/mobile are both needed
        NewUserPo newUserPo = (NewUserPo) baseCoder.code_sign(newUserBo,NewUserPo.class,codeFields,signFields,"signature");
        ReturnObject returnObject;
        returnObject=checkBloomFilter(newUserPo);
        try{
            stringBloomFilter.addValue(NAMEFILTER,newUserBo.getUserName());
            stringBloomFilter.addValue(EMAILFILTER,newUserBo.getEmail());
            stringBloomFilter.addValue(MOBILEFILTER,newUserBo.getMobile());
            newUserPo.setGmtCreate(LocalDateTime.now());
            newUserPoMapper.insert(newUserPo);
            returnObject=new ReturnObject<>(newUserPo);
            logger.debug("success trying to insert newUser");
        }
        //catch exception by unique index
        catch (DuplicateKeyException e){
            logger.debug("failed trying to insert newUser");
            //e.printStackTrace();
            String info=e.getMessage();
            if(info.contains("user_name_uindex")){
                return new ReturnObject(ReturnNo.USER_NAME_REGISTERED);
            }
            if(info.contains("email_uindex")){
                return new ReturnObject(ReturnNo.EMAIL_REGISTERED);
            }
            if(info.contains("mobile_uindex")){
                return new ReturnObject(ReturnNo.MOBILE_REGISTERED);
            }

        }
        catch (Exception e){
            logger.error("Internal error Happened:"+e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR, e.getMessage());
        }
        return returnObject;
    }
    /**
     * (物理) 删除新用户
     *
     * @param id 用户 id
     * @return 返回对象 ReturnObj
     * @author 24320182203227 Li Zihan
     */
    public ReturnObject<Object> physicallyDeleteUser(Long id) {
        ReturnObject<Object> retObj;
        try {
            int ret = newUserPoMapper.deleteByPrimaryKey(id);
            if (ret == 0) {
                logger.info("用户不存在或已被删除：id = " + id);
                retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
            } else {
                logger.info("用户 id = " + id + " 已被永久删除");
                retObj = new ReturnObject<>();
            }
        }
        catch (DataAccessException e)
        {
            logger.debug("sql exception : " + e.getMessage());
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("数据库错误：%s", e.getMessage()));
        }
        catch (Exception e) {
            // 其他Exception错误
            logger.error("other exception : " + e.getMessage());
            retObj = new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR, String.format("发生了严重的数据库错误：%s", e.getMessage()));
        }
        return retObj;
    }

    /**
     * ID获取用户信息
     * @author Li Zihan 24320182203227
     * @param id
     * @return 用户
     */
    public NewUserPo findNewUserById(Long id) {
        logger.debug("findUserById: Id =" + id);
        NewUserPo newUserPo = newUserPoMapper.selectByPrimaryKey(id);
        if (newUserPo == null) {
            logger.error("getNewUser: 新用户数据库不存在该用户 userid=" + id);
        }
        return newUserPo;
    }
    /**
     * @param userid   新用户id
     * @param departid 路径上的departid
     * @return boolean
     * @description 检查用户的departid是否与路径上的一致
     * Created by 22920192204219 蒋欣雨 at 2021/11/29
     */
    public boolean checkUserDid(Long userid, Long departid) {
        NewUserPo newUserPo = newUserPoMapper.selectByPrimaryKey(userid);
        if (newUserPo == null) {
            return false;
        }
        if (newUserPo.getDepartId() != departid) {
            return false;
        }
        return true;
    }

    /**
     * 获取新注册用户表
     * @param did
     * @param userName
     * @param mobile
     * @param email
     * @param page
     * @param pageSize
     * @return
     * @author BingShuai Liu 22920192204245
     */
    public ReturnObject selectAllNewUsers(Long did, String userName, String mobile, String email, Integer page, Integer pageSize){
        NewUserPoExample example = new NewUserPoExample();
        NewUserPoExample.Criteria criteria= example.createCriteria();
        PageHelper.startPage(page,pageSize);
        List<NewUserPo> newUserPos = new ArrayList<>();
        UserBo userBo = new UserBo();
        userBo.setDepartId(did);
        userBo.setUserName(userName);
        userBo.setMobile(mobile);
        userBo.setEmail(email);
        UserBo encryptedUserBo = (UserBo) baseCoder.code_sign(userBo,UserBo.class,codeFields,signFields,"signature");
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
            newUserPos = newUserPoMapper.selectByExample(example);
            // 验签
            for(NewUserPo newUserPo: newUserPos){
                if(null==baseCoder.decode_check(newUserPo,NewUserPo.class,codeFields,signFields,"signature")){
                    logger.error(ReturnNo.RESOURCE_FALSIFY.getMessage());
                    return new ReturnObject(ReturnNo.RESOURCE_FALSIFY);
                }
            }
            return new ReturnObject(newUserPos);
        }catch (DataAccessException e){
            return new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
    }
}

