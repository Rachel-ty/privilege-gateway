package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
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
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeVo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * 权限DAO
 * @author Ming Qiu
 **/
@Repository
public class PrivilegeDao implements InitializingBean {

    private  static  final Logger logger = LoggerFactory.getLogger(PrivilegeDao.class);


    @Autowired
    private PrivilegePoMapper poMapper;
    @Autowired
    private RolePrivilegePoMapper rolePrivilegePoMapper;

    @Autowired
    private RedisTemplate<String, Serializable> redisTemplate;



    @Autowired
    private BaseCoder coder;

    @Autowired
    private RedisUtil redisUtil;

    private final static String PRIVILEGESET_PATH="cn/edu/xmu/privilegegateway/privilegeservice/lua/SetPrivilege.lua";
    private final static String GETPID_PATH= "cn/edu/xmu/privilegegateway/privilegeservice/lua/JudgePIDByKey.lua";
    public final static String PRIVILEGEKEY = "P_%s_%d";
    public final static Collection<String> codeFields = new ArrayList<>();
    public final static List<String> signFields = new ArrayList<>(Arrays.asList("id","url", "requestType"));
    //功能角色
    public final static Byte FORBIDEN=1;
    public final static Byte NORMAL=0;
    public final static Byte MODIFIED=1;
    public final static Byte OK=0;

    /**
     * 将权限载入到本地缓存中
     * 如果未初始化，则初始话数据中的数据
     * @throws Exception
     * createdBy: Ming Qiu 2020-11-01 23:44
     * modifiedBy: Ming Qiu 2020-11-03 11:44
     *            将签名的认证改到Privilege对象中去完成
     *            Ming Qiu 2020-12-03 9:44
     *            将缓存放到redis中
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        PrivilegePoExample example = new PrivilegePoExample();
        List<PrivilegePo> privilegePos = poMapper.selectByExample(example);
        for (PrivilegePo po : privilegePos){
            Privilege priv = new Privilege(po);
            if (priv.authetic()) {
                logger.debug("afterPropertiesSet: key = " + priv.getKey() + " p = " + priv);
                redisTemplate.opsForHash().putIfAbsent("Priv", priv.getKey(), priv.getId());
            }else{
                logger.debug("afterPropertiesSet: id = " + priv.getId()+ ",Sign = "+priv.getSignature()+". cacuSign="+priv.getCacuSignature());
                logger.error("afterPropertiesSet: Wrong Signature(auth_privilege): id = " + priv.getId());
            }
        }
    }

    public void initialize() {
        PrivilegePoExample example = new PrivilegePoExample();
        List<PrivilegePo> privilegePos = poMapper.selectByExample(example);
        for (PrivilegePo po : privilegePos) {
            if (null == po.getSignature()) {
                Privilege priv = new Privilege(po);
                PrivilegePo newPo = new PrivilegePo();
                newPo.setId(po.getId());
                newPo.setSignature(priv.getCacuSignature());
                logger.debug("initialize: id = " + newPo.getId() + ",Sign = " + newPo.getSignature());
                poMapper.updateByPrimaryKeySelective(newPo);
            }
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
        StringBuffer key = new StringBuffer(url);
        key.append("-");
        key.append(requestType.getCode());
        logger.info("getPrivIdByKey: key = "+key.toString());
        return (Long) this.redisTemplate.opsForHash().get("Priv", key.toString());
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
     * @param mid
     * @param mname
     * @return
     */
    public ReturnObject changePriv(Privilege bo,Long mid,String mname){
        try {
            /* 验证数据是否重复 */
            String key=String.format(PRIVILEGEKEY,bo.getUrl(),bo.getRequestType().getCode());
            if(redisUtil.hasKey(key))
            {
                return new ReturnObject(ReturnNo.URL_SAME, "URL和RequestType不得与已有的数据重复");
            }
            else {
                PrivilegePoExample example = new PrivilegePoExample();
                PrivilegePoExample.Criteria criteria = example.createCriteria();
                criteria.andRequestTypeEqualTo(bo.getRequestType().getCode()).andUrlEqualTo(bo.getUrl());
                if (!poMapper.selectByExample(example).isEmpty()) {
                    return new ReturnObject(ReturnNo.URL_SAME, "URL和RequestType不得与已有的数据重复");
                }
            }
            /*验证数据*/
            PrivilegePo po = this.poMapper.selectByPrimaryKey(bo.getId());
            if (po == null)
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            PrivilegePo ret = (PrivilegePo)coder.decode_check(po, PrivilegePo.class, PrivilegeDao.codeFields, PrivilegeDao.signFields, "signature");
            if (ret.getSignature()== null) {
                return new ReturnObject(ReturnNo.RESOURCE_FALSIFY, "该权限可能被篡改，请联系管理员处理");
            }
            /* 开始更新 */
            PrivilegePo newPo =(PrivilegePo) coder.code_sign(po,PrivilegePo.class,codeFields,signFields,"signature");
            Common.setPoModifiedFields(newPo,mid,mname);
            this.poMapper.updateByPrimaryKeySelective(newPo);
            DefaultRedisScript script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource(PRIVILEGESET_PATH)));
            List<String> keys = Stream.of(key).collect(Collectors.toList());
            redisUtil.executeScript(script,keys,newPo.getId());
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e)
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
     * @param vo
     * @param creatorid
     * @param creatorname
     * @return
     */
    public ReturnObject addPriv(PrivilegeVo vo,Long creatorid,String creatorname)
    {
        try {
                String key=String.format(PRIVILEGEKEY,vo.getUrl(),vo.getRequestType());
                if(redisUtil.hasKey(key))
                    return new ReturnObject(ReturnNo.PRIVILEGE_RELATION_EXIST);
                PrivilegePo po=(PrivilegePo)coder.code_sign(vo,PrivilegePo.class,codeFields,signFields,"signature");
                Common.setPoCreatedFields(po,creatorid,creatorname);
                int ret=poMapper.insertSelective(po);
                DefaultRedisScript script = new DefaultRedisScript<>();
                script.setScriptSource(new ResourceScriptSource(new ClassPathResource(PRIVILEGESET_PATH)));
                List<String> keys = Stream.of(key).collect(Collectors.toList());
                redisUtil.executeScript(script,keys,po.getId());
                PrivilegeRetVo retVo=(PrivilegeRetVo)Common.cloneVo(po, PrivilegeRetVo.class);
                if(retVo!=null)
                {
                    retVo.setCreator(new AdminVo(po.getCreatorId(),po.getCreatorName(),0));
                    retVo.setModifier(new AdminVo(po.getModifierId(),po.getModifierName(),0));
                    retVo.setSign(0);
                }
                return new ReturnObject(retVo);
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.PRIVILEGE_RELATION_EXIST,String.format("重复定义",e.getMessage()));
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
            List<PrivilegePo> polist =new ArrayList<>();
            Long pid=getPrivIdByKey(url,type);
            if(pid!=null)
                polist.add(poMapper.selectByPrimaryKey(pid));
            else {
                PrivilegePoExample example = new PrivilegePoExample();
                PrivilegePoExample.Criteria criteria = example.createCriteria();
                criteria.andUrlEqualTo(url);
                criteria.andRequestTypeEqualTo(type);
                polist = poMapper.selectByExample(example);
            }
            List<PrivilegeRetVo> vo=new ArrayList<>(polist.size());
            PageHelper.startPage(pagenum, pagesize);
            for(PrivilegePo po:polist)
            {
                PrivilegePo newpo=(PrivilegePo)coder.decode_check(po,null,codeFields,signFields,"signature");
                int sign=OK;
                if(newpo.getSignature()==null)
                {
                    sign=MODIFIED;
                }
                PrivilegeRetVo retVo=(PrivilegeRetVo) Common.cloneVo(newpo,PrivilegeRetVo.class);
                if(retVo!=null)
                {
                    retVo.setCreator(new AdminVo(po.getCreatorId(),po.getCreatorName(),sign));
                    retVo.setModifier(new AdminVo(po.getModifierId(),po.getModifierName(),sign));
                    retVo.setSign(sign);
                }
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
                return new ReturnObject(ReturnNo.OK);
            String privkey=String.format(PRIVILEGEKEY,po.getUrl(),po.getRequestType());
            if(redisUtil.hasKey(privkey))
            {
                redisUtil.del(privkey);
            }
            List<RolePrivilegePo> rolePrivilegePos=selectByPrivid(pid);
            for(RolePrivilegePo rolePrivilegePo:rolePrivilegePos)
            {
                String brkey=String.format(RoleDao.BASEROLEKEY,rolePrivilegePo.getRoleId());
                redisUtil.del(brkey);
                rolePrivilegePoMapper.deleteByPrimaryKey(rolePrivilegePo.getId());
            }
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST, String.format("删除权限失败",e.getMessage()));
        }
        return new ReturnObject(ReturnNo.OK);
    }
    /*禁用权限*/

    /**
     * @author zhangyu
     * @param pid
     * @return
     */

    public ReturnObject forbidPriv(Long pid,Long mid,String mname)
    {
        try {
            //判断是否为空
            PrivilegePo po = poMapper.selectByPrimaryKey(pid);
            if(po==null)
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            po.setState(FORBIDEN);
            Common.setPoModifiedFields(po,mid,mname);
            poMapper.updateByPrimaryKey(po);
            //清除对应的redis缓存
            String key=String.format(PRIVILEGEKEY,po.getUrl(),po.getRequestType());
            if(redisUtil.hasKey(key))
                redisUtil.del(key);
            List<Long> pos=GetRoleIdByPrivId(pid);
            for(Long roleid:pos)
            {
                String rpkey=String.format(RoleDao.BASEROLEKEY,roleid);
                redisUtil.del(rpkey);
            }
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,String.format("读取数据库错误",e.getMessage()));
        }
    }

    /**
     * 解禁权限
     * @author zhangyu
     * @param pid
     * @return
     */
    public ReturnObject releasePriv(Long pid,Long mid,String mname)
    {
        try
        {
            //判断是否为空
            PrivilegePo po = poMapper.selectByPrimaryKey(pid);
            if(po==null)
                return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
            po.setState(NORMAL);
            Common.setPoModifiedFields(po,mid,mname);
            poMapper.updateByPrimaryKeySelective(po);
            String key=String.format(PRIVILEGEKEY,po.getUrl(),po.getRequestType());
            DefaultRedisScript script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource(PRIVILEGESET_PATH)));
            List<String> keys = Stream.of(key).collect(Collectors.toList());
            redisUtil.executeScript(script,keys,pid);
            List<Long> pos=GetRoleIdByPrivId(pid);
            for(Long roleid:pos)
            {
                String rpkey=String.format(RoleDao.BASEROLEKEY,roleid);
                redisUtil.addSet(rpkey,pid);
            }
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR,String.format("读取数据库错误",e.getMessage()));
        }
    }
    public List<RolePrivilegePo> selectByPrivid(Long Privid)
    {
        try
        {
            RolePrivilegePoExample example=new RolePrivilegePoExample();
            RolePrivilegePoExample.Criteria criteria=example.createCriteria();
            criteria.andPrivilegeIdEqualTo(Privid);
            List<RolePrivilegePo> pos=rolePrivilegePoMapper.selectByExample(example);
            List<RolePrivilegePo> newpos=new ArrayList<>(pos.size());
            for(RolePrivilegePo po:pos)
            {
                RolePrivilegePo newpo=(RolePrivilegePo) coder.code_sign(po,RolePrivilegePo.class,codeFields,signFields,"signature");
                newpos.add(po);
            }
            return newpos;
        }catch (Exception e)
        {
            return null;
        }
    }

    public List<Long> GetRoleIdByPrivId(Long Privid)
    {
        try
        {
            RolePrivilegePoExample example=new RolePrivilegePoExample();
            RolePrivilegePoExample.Criteria criteria=example.createCriteria();
            criteria.andPrivilegeIdEqualTo(Privid);
            List<RolePrivilegePo> pos=rolePrivilegePoMapper.selectByExample(example);
            List<Long> newpos=new ArrayList<>(pos.size());
            for(RolePrivilegePo po:pos)
            {
                RolePrivilegePo newpo=(RolePrivilegePo) coder.code_sign(po,RolePrivilegePo.class,codeFields,signFields,"signature");
                newpos.add(newpo.getRoleId());
            }
            return newpos;
        }catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 权限的影响力分析
     * 任务3-7
     * 删除和禁用某个权限时，删除所以影响的role，group和user的redisKey
     * @param privId 权限id
     * @return 影响的role，group和user的redisKey
     */
    public List<String> privilegeImpact(Long privId){
        return null;
    }

}
