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

package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.*;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.PrivilegePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.RolePrivilegePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.PrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.PrivilegePoExample;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePoExample;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.AdminVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.BasePrivilegeRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePoExample;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * 权限DAO
 * @author Ming Qiu
 **/
@Repository
public class PrivilegeDao {

    private  static  final Logger logger = LoggerFactory.getLogger(PrivilegeDao.class);

    @Value("${privilegeservice.role.expiretime}")
    private long timeout;

    @Autowired
    private PrivilegePoMapper poMapper;

    @Autowired
    private RolePrivilegePoMapper rolePrivilegePoMapper;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BaseCoder baseCoder;

    final static List<String> newRolePrivilegeSignFields = new ArrayList<>(Arrays.asList("roleId", "privilegeId"));
    final static List<String> privilegeSignFields = new ArrayList<>(Arrays.asList("id", "url","requestType"));

    private static final String PRIVKEY = "%s-%d";



    @Autowired
    private BaseCoder coder;

    //功能角色
    public final static Byte FORBIDEN=1;
    public final static Byte NORMAL=0;
    public final static Integer MODIFIED=1;
    public final static Integer NOTMODIFIED=0;

    /**
     * 重写签名和加密
     * @author Ming Qiu
     * date： 2021/12/04 16:01
     */
    public void initialize() {
        PrivilegePoExample example = new PrivilegePoExample();
        List<PrivilegePo> privilegePos = poMapper.selectByExample(example);
        for (PrivilegePo po : privilegePos) {
            PrivilegePo newPo = (PrivilegePo) baseCoder.code_sign(po, PrivilegePo.class, null, privilegeSignFields, "signature");
            logger.debug("initialize: id = " + newPo.getId() + ",Sign = " + newPo.getSignature());
            poMapper.updateByPrimaryKeySelective(newPo);
        }

        RolePrivilegePoExample example1 = new RolePrivilegePoExample();
        List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(example1);
        for (RolePrivilegePo po : rolePrivilegePos) {
            RolePrivilegePo newPo = (RolePrivilegePo) baseCoder.code_sign(po, RolePrivilegePo.class, null, newRolePrivilegeSignFields, "signature");
            rolePrivilegePoMapper.updateByPrimaryKeySelective(newPo);
        }
    }

    /**
     * 以url和RequestType获得缓存的Privilege id
     * @param url: 访问链接
     * @param requestType: 访问类型
     * @return id Privilege id
     * createdBy: Ming Qiu 2020-11-01 23:44
     */
    public Long getPrivIdByKey(String url, Privilege.RequestType requestType){
        String key = String.format(PRIVKEY, url, requestType.getCode());
        logger.info("getPrivIdByKey: key = "+key.toString());
        return (Long) this.redisUtil.get(key);
    }

    /**
     * 根据权限Id查询权限
     * @author yue hao
     * @param id 权限ID
     * @return 权限
     */
    public Privilege findPriv(Long id){
        PrivilegePo po = poMapper.selectByPrimaryKey(id);
        Privilege priv = new Privilege(po);
        if (priv.authetic()) {
            return priv;
        }
        else {
            logger.error("findPriv: Wrong Signature(auth_privilege): id =" + po.getId());
            return null;
        }
    }
    /**
     * 查询所有权限
     * @param page: 页码
     * @param pageSize : 每页数量
     * @return 权限列表
     */
    public ReturnObject<PageInfo<VoObject>> findAllPrivs(Integer page, Integer pageSize){
        PrivilegePoExample example = new PrivilegePoExample();
        PrivilegePoExample.Criteria criteria = example.createCriteria();
        PageHelper.startPage(page, pageSize);
        List<PrivilegePo> privilegePos = null;
        try {
            privilegePos = poMapper.selectByExample(example);
        }catch (DataAccessException e){
            logger.error("findAllPrivs: DataAccessException:" + e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR);
        }

        List<VoObject> ret = new ArrayList<>(privilegePos.size());
        for (PrivilegePo po : privilegePos) {
            Privilege priv = new Privilege(po);
            if (priv.authetic()) {
                logger.debug("findAllPrivs: key = " + priv.getKey() + " p = " + priv);
                ret.add(priv);
            }else{
                logger.error("findAllPrivs: 信息签名错误：id = "+po.getId());
            }

        }
        PageInfo<PrivilegePo> privPoPage = PageInfo.of(privilegePos);
        PageInfo<VoObject> privPage = new PageInfo<>(ret);
        privPage.setPages(privPoPage.getPages());
        privPage.setPageNum(privPoPage.getPageNum());
        privPage.setPageSize(privPoPage.getPageSize());
        privPage.setTotal(privPoPage.getTotal());
        return new ReturnObject<>(privPage);
    }

    /**
     * 修改权限
     * @modifiedBy 24320182203266
     * modified by zhangyu
     */
    /**
     *
     * @param bo
     * @return
     */
    public ReturnObject changePriv(Privilege bo){
        try {
            //没有对应字段，会跳过的，如果不进行加密，生成签名直接放进去会破坏里面的加密
            PrivilegePo po=(PrivilegePo) coder.code_sign(bo,PrivilegePo.class,codeFields,signFields,"signature");
            poMapper.insertSelective(po);
            PrivilegePo retpo=poMapper.selectByPrimaryKey(po.getId());
            //需要先解密再加密，不然多重加密
            PrivilegePo newpo=(PrivilegePo)coder.decode_check(retpo,PrivilegePo.class,codeFields,signFields,"signature");
            PrivilegePo updatepo=(PrivilegePo)coder.code_sign(newpo,PrivilegePo.class,codeFields,signFields,"signature");
            poMapper.updateByPrimaryKeySelective(updatepo);
            List<String> keys=privilegeImpact(po.getId());
            for(String key:keys)
            {
                redisUtil.del(key);
            }
            return new ReturnObject(ReturnNo.OK);
        }catch (DuplicateFormatFlagsException e)
        {
            return new ReturnObject(ReturnNo.URL_SAME);
        }
        catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }
    /*新建权限

     */

    /**
     *
     */
    /**
     * 新增权限
     * @author: zhangyu
     */
    /**
     *
     * @param privilege
     * @return
     */
    public ReturnObject addPriv(Privilege privilege)
    {
        try {

                PrivilegePo po=(PrivilegePo) Common.cloneVo(privilege,PrivilegePo.class);
                poMapper.insertSelective(po);
                PrivilegePo newpo=(PrivilegePo)coder.code_sign(po,PrivilegePo.class,codeFields,signFields,"signature");
                poMapper.insertSelective(newpo);
                PrivilegePo retpo=poMapper.selectByPrimaryKey(po.getId());
                PrivilegePo retnewpo=(PrivilegePo)coder.decode_check(retpo,PrivilegePo.class,codeFields,signFields,"signature");
                String key=String.format(PRIVILEGEKEY,retnewpo.getUrl(),retnewpo.getRequestType());
                redisUtil.set(key, retnewpo.getId(),timeout);
                PrivilegeRetVo retVo=(PrivilegeRetVo)Common.cloneVo(po, PrivilegeRetVo.class);
                if(retnewpo.getSignature()!=null)
                {
                    retVo.setSign(OK);
                }
                else
                {
                    retVo.setSign(MODIFIED);
                }
                return new ReturnObject(retVo);
        }catch (DuplicateFormatFlagsException e)
        {
            return new ReturnObject(ReturnNo.PRIVILEGE_RELATION_EXIST,String.format("重复定义",e.getMessage()));
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }

    }
    /*获取权限*/

    /**
     * @author zhangyu
     * @param url
     * @param type
     * @param pagenum
     * @param pagesize
     * @return
     */
    public ReturnObject getPriv(String url ,Byte type,Integer pagenum,Integer pagesize)
    {
        try {
            PageHelper.startPage(pagenum, pagesize);
            PrivilegePoExample example = new PrivilegePoExample();
            PrivilegePoExample.Criteria criteria = example.createCriteria();
            criteria.andUrlEqualTo(url);
            criteria.andRequestTypeEqualTo(type);
            List<PrivilegePo> polist = poMapper.selectByExample(example);
            List<PrivilegeRetVo> vo=new ArrayList<>(polist.size());
            List<PrivilegePo> newpolist=Common.listDecode(polist,PrivilegePo.class,coder,codeFields,signFields,"signature");
            for(PrivilegePo po:newpolist)
            {
                int sign=OK;
                if(po.getSignature()==null)
                {
                    sign=MODIFIED;
                }
                PrivilegeRetVo retVo=(PrivilegeRetVo) Common.cloneVo(po,PrivilegeRetVo.class);
                retVo.setSign(sign);
                vo.add(retVo);
            }
            PageInfo<BasePrivilegeRetVo> pageInfo=new PageInfo(vo);
            ReturnObject returnObject= new ReturnObject(pageInfo);
            return Common.getPageRetVo(returnObject,PrivilegeRetVo.class);
        }catch (Exception e)
        {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,String.format("数据库错误",e.getMessage()));
        }
    }
    /*删除权限*/

    /**
     * @author zhangyu
     * @param pid
     * @return
     */
    public ReturnObject delPriv(Long pid)
    {
        try
        {
            PrivilegePo po=poMapper.selectByPrimaryKey(pid);
            if(po==null)
            {
                return new ReturnObject(ReturnNo.OK);
            }
            String privkey=String.format(PRIVKEY,po.getUrl(),po.getRequestType());
            if(redisUtil.hasKey(privkey))
            {
                redisUtil.del(privkey);
            }
            Collection<String> keys=privilegeImpact(pid);
            for(String key:keys)
            {
                redisUtil.del(key);
            }
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, String.format("删除权限失败",e.getMessage()));
        }
    }
    /**
     * 由Role Id 获得 Privilege Id 列表
     *
     * @param id: Role id
     * @return Privilege Id 列表
     * created by Ming Qiu in 2020/11/3 11:48
     */
    public ReturnObject getPrivIdsByRoleId(Long id) {
        try{
            RolePrivilegePoExample example = new RolePrivilegePoExample();
            RolePrivilegePoExample.Criteria criteria = example.createCriteria();
            criteria.andRoleIdEqualTo(id);
            List<RolePrivilegePo> rolePrivilegePos = rolePrivilegePoMapper.selectByExample(example);
            List<Long> retIds = new ArrayList<>(rolePrivilegePos.size());
            for (RolePrivilegePo po : rolePrivilegePos) {
                RolePrivilegePo newPo = (RolePrivilegePo) baseCoder.decode_check(po,RolePrivilegePo.class,null,newRolePrivilegeSignFields,"signature");
                if (newPo.getSignature()==null) {
                    logger.debug("getPrivIdsBByRoleId: roleId = " + po.getRoleId() + " privId = " + po.getPrivilegeId());
                }
                retIds.add(po.getPrivilegeId());
            }
            return new ReturnObject(retIds);
        }catch(Exception e){
            logger.error("getPrivIdsBByRoleId: "+e.getMessage());
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }

    /**
     * 权限的影响力分析
     * 任务3-7
     * 删除和禁用某个权限时，返回所有影响的role，group和user的redisKey
     * @param privId 权限id
     * @return 影响的role，group和user的redisKey
     */
    public Collection<String> privilegeImpact(Long privId){
        return null;
    }

    /**
     * 将一个角色的所有权限id载入到Redis
     *
     * @param id 角色id
     * @return void
     *
     * createdBy: Ming Qiu 2020-11-02 11:44
     * ModifiedBy: Ming Qiu 2020-11-03 12:24
     * 将读取权限id的代码独立为getPrivIdsByRoleId. 增加redis值的有效期
     *            Ming Qiu 2020-11-07 8:00
     * 集合里强制加“0”
     */
    public ReturnObject loadBaseRolePriv(Long id) {
        try{
            ReturnObject returnObject = getPrivIdsByRoleId(id);
            if(returnObject.getCode()!=ReturnNo.OK){
                return returnObject;
            }
            List<Long> privIds = (List<Long>) returnObject.getData();
            String key = String.format(RoleDao.BASEROLEKEY, id);
            for (Long pId : privIds) {
                redisUtil.addSet(key, pId);
            }
            long randTimeout = Common.addRandomTime(timeout);
            redisUtil.addSet(key,0);
            redisUtil.expire(key, randTimeout, TimeUnit.SECONDS);
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e){
            logger.error("loadBaseRolePriv:"+e.getMessage());
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }

    }
    public InternalReturnObject loadPrivilege(String url,Byte requestType) {
        try {
            PrivilegePoExample example = new PrivilegePoExample();
            PrivilegePoExample.Criteria criteria = example.createCriteria();
            criteria.andRequestTypeEqualTo(requestType);
            criteria.andUrlEqualTo(url);
            List<PrivilegePo> poList = poMapper.selectByExample(example);
            for(PrivilegePo po:poList)
            {
                String key=String.format(PRIVKEY,po.getUrl(),po.getRequestType());
                redisUtil.addSet(key,po.getId());
            }
            return new InternalReturnObject(ReturnNo.OK);
        }catch (Exception e)
        {
            return new InternalReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }

    }
}
