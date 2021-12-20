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
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.*;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.*;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePoExample;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeVo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.*;

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
    @Lazy
    private RoleDao roleDao;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BaseCoder baseCoder;

    final static List<String> newRolePrivilegeSignFields = new ArrayList<>(Arrays.asList("roleId", "privilegeId"));
    final static List<String> privilegeSignFields = new ArrayList<>(Arrays.asList("id", "url","requestType"));

    public static final String PRIVKEY = "%s-%d";
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
     *修改权限
     * @modifiedBy 24320182203266
     * @modifiedBy by zhangyu
     * @param bo
     * @return
     */
    public ReturnObject changePriv(Privilege bo){
        try {
            PrivilegePo po=poMapper.selectByPrimaryKey(bo.getId());
            if(po==null)
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
            po.setState(bo.getState());
            po.setUrl(bo.getUrl());
            po.setRequestType(bo.getRequestType().getCode());
            po.setName(bo.getName());
            //生成签名
            PrivilegePo newpo=(PrivilegePo)baseCoder.code_sign(po,PrivilegePo.class,null,privilegeSignFields,"signature");
            poMapper.updateByPrimaryKeySelective(newpo);
            //清除缓存中的权限
            String pkey=String.format(PRIVKEY,po.getUrl(),po.getRequestType());
            redisUtil.del(pkey);
            return new ReturnObject(ReturnNo.OK);
        }catch (DuplicateKeyException e)
        {
            return new ReturnObject(ReturnNo.URL_SAME);
        }
        catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }

    /**
     * 修改权限状态
     * @creator zhangyu
     * @param bo
     * @return
     */
    public ReturnObject changePrivState(Privilege bo)
    {
        try {
            Collection<String> keys = privilegeImpact(bo.getId());
            PrivilegePo po = poMapper.selectByPrimaryKey(bo.getId());
            if (po == null)
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
            po.setState(bo.getState());
            //生成签名
            PrivilegePo newpo = (PrivilegePo) baseCoder.code_sign(po, PrivilegePo.class, null, privilegeSignFields, "signature");
            poMapper.updateByPrimaryKeySelective(newpo);
            for (String key : keys) {
                redisUtil.del(key);
            }
            //清除缓存中的权限
            String pkey = String.format(PRIVKEY, po.getUrl(), po.getRequestType());
            redisUtil.del(pkey);
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }

    }
    /**
     * 新增权限
     * @author: zhangyu
     * @param privilege
     * @return
     */
    public ReturnObject addPriv(Privilege privilege)
    {
        try {

            PrivilegePo po=(PrivilegePo) Common.cloneVo(privilege,PrivilegePo.class);
            poMapper.insertSelective(po);
            PrivilegePo newpo=(PrivilegePo)baseCoder.code_sign(po,PrivilegePo.class,null,privilegeSignFields,"signature");
            poMapper.updateByPrimaryKeySelective(newpo);
            PrivilegePo retpo=poMapper.selectByPrimaryKey(po.getId());
            PrivilegeRetVo retVo=(PrivilegeRetVo)Common.cloneVo(po, PrivilegeRetVo.class);
            retVo.setSign(NOTMODIFIED);
            return new ReturnObject(retVo);
        }catch (DuplicateKeyException e)
        {
            return new ReturnObject(ReturnNo.URL_SAME,"权限url/RequestType重复");
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }

    }

    /**
     * 获取权限
     * @author zhangyu
     * @param url
     * @param type  请求类型
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
            Collection<PrivilegePo> newpolist=Common.listDecode(polist,PrivilegePo.class,baseCoder,null,privilegeSignFields,"signature",true);
            for(PrivilegePo po:newpolist)
            {
                int sign=NOTMODIFIED;
                if(po.getSignature()==null)
                {
                    sign=MODIFIED;
                }
                PrivilegeRetVo retVo=(PrivilegeRetVo) Common.cloneVo(po,PrivilegeRetVo.class);
                retVo.setSign(sign);
                vo.add(retVo);
            }
            PageInfo pageInfo = PageInfo.of(polist);
            pageInfo.setList(vo);
            ReturnObject returnObject= new ReturnObject(pageInfo);
            return Common.getPageRetVo(returnObject,PrivilegeRetVo.class);
        }catch (Exception e)
        {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,String.format("数据库错误",e.getMessage()));
        }
    }
    /**
     * 删除权限
     * @author zhangyu
     * @param pid 权限id
     * @return
     */
    public ReturnObject delPriv(Long pid)
    {
        try
        {
            Collection<String> keys=privilegeImpact(pid);
            PrivilegePo po=poMapper.selectByPrimaryKey(pid);
            if(po==null)
            {
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            }
            poMapper.deleteByPrimaryKey(pid);
            RolePrivilegePoExample example=new RolePrivilegePoExample();
            RolePrivilegePoExample.Criteria criteria=example.createCriteria();
            criteria.andPrivilegeIdEqualTo(pid);
            rolePrivilegePoMapper.deleteByExample(example);
            String privkey=String.format(PRIVKEY,po.getUrl(),po.getRequestType());
            if(redisUtil.hasKey(privkey))
            {
                redisUtil.del(privkey);
            }
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
     * @author zhangyu
     * @param roleid
     * @param privilegeid
     * @param creatorid
     * @param creatorname
     * @return
     */
    public ReturnObject addBaseRolePriv(Long roleid,Long privilegeid,Long creatorid,String creatorname){
        try
        {
            Collection<String> keys=roleDao.roleImpact(roleid);
            RolePrivilegePo rolePrivilegePo=new RolePrivilegePo();
            rolePrivilegePo.setRoleId(roleid);
            rolePrivilegePo.setPrivilegeId(privilegeid);
            Common.setPoCreatedFields(rolePrivilegePo,creatorid,creatorname);
            PrivilegePo privilege=poMapper.selectByPrimaryKey(privilegeid);
            if(privilege==null)
            {
                return new ReturnObject(ReturnNo.PRIVILEGE_RELATION_EXIST);
            }
            RolePrivilegePo newpo=(RolePrivilegePo)baseCoder.code_sign(rolePrivilegePo,RolePrivilegePo.class,null,newRolePrivilegeSignFields,"signature");
            rolePrivilegePoMapper.insertSelective(newpo);
            RolePrivilegePo retpo=rolePrivilegePoMapper.selectByPrimaryKey(newpo.getId());
            //判断关系是否篡改
            RolePrivilegePo newretpo=(RolePrivilegePo) baseCoder.decode_check(retpo,RolePrivilegePo.class,null,newRolePrivilegeSignFields,"signature");
            BaseRolePrivilegeRetVo vo=(BaseRolePrivilegeRetVo) Common.cloneVo(newretpo,BaseRolePrivilegeRetVo.class);
            //判断权限是否篡改
            PrivilegePo newprivilege=(PrivilegePo)baseCoder.decode_check(privilege,PrivilegePo.class,null,privilegeSignFields,"signature");
            Integer sign=NOTMODIFIED;
            //判断关系是否篡改                    权限是否篡改
            if(newretpo.getSignature()==null||newprivilege.getSignature()==null)
            {
                sign=MODIFIED;
            }
            vo.setSign(sign);
            vo.setName(privilege.getName());
            vo.setId(privilege.getId());
            for(String key:keys)
            {
                redisUtil.del(key);
            }
            return new ReturnObject(vo);

        }catch (DuplicateKeyException e)
        {
            return new ReturnObject(ReturnNo.PRIVILEGE_RELATION_EXIST,"重复定义权限");
        }
        catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }

    }
    /**
     * 由角色id,privilegeid删除角色对应权限
     * @author zhangyu
     * @param rid
     * @param pid
     * @return
     */
    public ReturnObject delRolePriv(Long rid,Long pid){
        try {
            Collection<String> keys=roleDao.roleImpact(rid);
            RolePrivilegePoExample example = new RolePrivilegePoExample();
            RolePrivilegePoExample.Criteria criteria = example.createCriteria();
            criteria.andRoleIdEqualTo(rid);
            criteria.andPrivilegeIdEqualTo(pid);
            rolePrivilegePoMapper.deleteByExample(example);
            //删除缓存功能角色权限
            String key=String.format(RoleDao.BASEROLEKEY,rid);
            redisUtil.del(key);
            //删除角色相关
            for(String rkey:keys)
            {
                redisUtil.del(rkey);
            }
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }
    }
    /**
     * @author: zhang yu
     * @date: 2021/11/25 20:04
     * @version: 1.0
     */
    /**
     * 查询功能角色权限，先根据roleid查询privilegeid,再根据privilegeidc查询
     *
     * @param rid
     * @param pagenum
     * @param pagesize
     * @return
     */
    public ReturnObject selectBaseRolePrivs(Long rid, Integer pagenum, Integer pagesize)
    {
        try{
            PageHelper.startPage(pagenum,pagesize);
            RolePrivilegePoExample example=new RolePrivilegePoExample();
            RolePrivilegePoExample.Criteria criteria=example.createCriteria();
            criteria.andRoleIdEqualTo(rid);
            List<RolePrivilegePo> pos=rolePrivilegePoMapper.selectByExample(example);
            //判断是否篡改
            List<RolePrivilegePo> newpos=Common.listDecode(pos,RolePrivilegePo.class,baseCoder,null,newRolePrivilegeSignFields,"signature",true);
            List<BaseRolePrivilegeRetVo> volist=new ArrayList<>();
            for(RolePrivilegePo po:newpos)
            {
                //查询对应的权限信息
                PrivilegePo privilege=poMapper.selectByPrimaryKey(po.getPrivilegeId());
                //判断权限是否修改
                BaseRolePrivilegeRetVo vo=(BaseRolePrivilegeRetVo)Common.cloneVo(po,BaseRolePrivilegeRetVo.class);
                Integer sign=NOTMODIFIED;
                if(privilege.getSignature()==null||po.getSignature()==null)
                {
                    sign=MODIFIED;
                }
                vo.setSign(sign);
                vo.setName(privilege.getName());
                vo.setId(privilege.getId());
                volist.add(vo);
            }
            PageInfo pageInfo = PageInfo.of(newpos);
            pageInfo.setList(volist);
            ReturnObject returnObject= new ReturnObject(pageInfo);
            return Common.getPageRetVo(returnObject,BaseRolePrivilegeRetVo.class);
        }catch(Exception e)
        {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
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
     * @author zihan zhou 19720192203768
     * @param privId 权限id
     * @return 影响的role，group和user的redisKey
     */
    public Collection<String> privilegeImpact(Long privId){
        List<Long> roleIdList =findRoleId(privId);
        Set<String> resultSet=new HashSet<>();
        for (Long roleId : roleIdList) {
            Collection<String> roleImpact=roleDao.roleImpact(roleId);
            resultSet.addAll(roleImpact);
        }
        return resultSet;
    }

    public List<Long> findRoleId(Long privId) {
        RolePrivilegePoExample example = new RolePrivilegePoExample();
        RolePrivilegePoExample.Criteria criteria = example.createCriteria();
        criteria.andPrivilegeIdEqualTo(privId);
        List<RolePrivilegePo> gList = rolePrivilegePoMapper.selectByExample(example);
        if(gList==null||gList.size()==0){
            return new ArrayList<>();
        }
        List<Long> resultList = new ArrayList<>();
        for (RolePrivilegePo po : gList) {
            resultList.add(po.getRoleId());
        }
        return resultList;
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
    public ReturnObject loadPrivilege(String url,Byte requestType) {
        try {
            PrivilegePoExample example = new PrivilegePoExample();
            PrivilegePoExample.Criteria criteria = example.createCriteria();
            criteria.andRequestTypeEqualTo(requestType);
            criteria.andUrlEqualTo(url);
            List<PrivilegePo> poList = poMapper.selectByExample(example);
            for(PrivilegePo po:poList)
            {
                String key=String.format(PRIVKEY,po.getUrl(),po.getRequestType());
                logger.info("redis-set:"+key);
                redisUtil.set(key,po.getId(),timeout);
            }
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,e.getMessage());
        }

    }
    /**
     * 通过角色id删除对应的角色权限
     * @return ReturnObject
     * @author 张晖婧
     * */
    public int deleteRolePrivByRoleId(Long roleId) {
        RolePrivilegePoExample exampleRP = new RolePrivilegePoExample();
        RolePrivilegePoExample.Criteria criteriaRP = exampleRP.createCriteria();
        criteriaRP.andRoleIdEqualTo(roleId);
        return rolePrivilegePoMapper.deleteByExample(exampleRP);
    }

}
