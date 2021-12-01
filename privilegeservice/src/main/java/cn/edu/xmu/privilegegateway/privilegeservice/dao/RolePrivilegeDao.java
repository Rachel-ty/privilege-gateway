package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.PrivilegePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.RolePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.RolePrivilegePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.PrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePrivilegePoExample;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.AdminVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.BasePrivilegeRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.BaseRolePrivilegeRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.SimpleBaseRolePrivlegeVo;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author: zhang yu
 * @date: 2021/11/30 21:27
 * @version: 1.0
 * 角色权限
*/
@Repository
public class RolePrivilegeDao {
    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BaseCoder coder;//使用basecoder,便于日后进行加密（现在还没有加密）

    @Autowired
    private RolePoMapper roleMapper;

    @Autowired
    private RolePrivilegePoMapper rolePrivilegePoMapper;

    @Autowired
    private PrivilegePoMapper privilegePoMapper;

    @Autowired
    private PrivilegeDao privDao;

    public final static Byte ISBASEROLE=1;
    public final static Byte ISFORBIDEN=1;
    //功能角色
    public final static String BASEROLEKEY="br_%d";
    public final static Collection<String> codeFields = new ArrayList<>();
    public final static List<String> signFields = new ArrayList<>(Arrays.asList("roleId", "privilegeId", "creatorId"));

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
            RolePo rolePo=null;
            rolePo=roleMapper.selectByPrimaryKey(rid);
            if(rolePo==null||rolePo.getBaserole()!=ISBASEROLE)
            {
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
            }
            String key=String.format(BASEROLEKEY,rid);
            PageHelper.startPage(pagenum, pagesize);
            Set<Long> PrividSet=new HashSet<>();
            List<BasePrivilegeRetVo> voList=new ArrayList<BasePrivilegeRetVo>();
            List<PrivilegePo> polist=new ArrayList<>();
            List<RolePrivilegePo> rolePrivilegePos =new ArrayList<>();
            if(redisUtil.hasKey(key))
            {
               var ret=redisUtil.getSet(key);
              for(Serializable r:ret)
              {
                  Long newr=(Long)r;
                  PrividSet.add(newr);
              }
            }
            else {
                RolePrivilegePoExample example = new RolePrivilegePoExample();
                RolePrivilegePoExample.Criteria criteria = example.createCriteria();
                criteria.andRoleIdEqualTo(rid);
                rolePrivilegePos = rolePrivilegePoMapper.selectByExample(example);
                for(RolePrivilegePo po:rolePrivilegePos)
                    PrividSet.add(po.getPrivilegeId());
            }

            for (Long pid : PrividSet) {
                    PrivilegePo privilege=privDao.findPrivPo(pid);
                    if(privilege.getState()!=ISFORBIDEN)
                    {
                        BasePrivilegeRetVo vo=(BasePrivilegeRetVo) Common.cloneVo(privilege,BasePrivilegeRetVo.class);
                        int sign=0;
                        if(privilege.getSignature()==null)
                            sign=1;
                        vo.setSign(sign);
                        vo.setCreator(new AdminVo(privilege.getCreatorId(),privilege.getCreatorName(),sign));
                        vo.setModifier(new AdminVo(privilege.getModifierId(),privilege.getModifierName(),sign));
                        voList.add(vo);
                    }
                }
            PageInfo<BasePrivilegeRetVo> rolePage = PageInfo.of(voList);
            ReturnObject returnObject=new ReturnObject(rolePage);
            return Common.getPageRetVo(returnObject,BasePrivilegeRetVo.class);
        }catch(Exception e)
        {
            return new ReturnObject<>(ReturnNo.INTERNAL_SERVER_ERR,String.format("数据库发生错误",e.getMessage()));
        }
    }

    /**
     * 由Role Id, Privilege Id 增加功能角色权限
     * created by 王琛 24320182203277
     * modified by zhangyu
     */
    /**
     * @param vo
     * @return
     */
    public ReturnObject<Object> addBaseRolePriv(SimpleBaseRolePrivlegeVo vo){
        RolePrivilegePo po=(RolePrivilegePo) coder.code_sign(vo,RolePrivilegePo.class,codeFields,signFields,"signature");
        //获取当前时间
        LocalDateTime localDateTime = LocalDateTime.now();
        po.setGmtCreate(localDateTime);
        //查询是否有对应的角色和权限
        try
        {
            RolePo rolePo=roleMapper.selectByPrimaryKey(vo.getRoleId());
            if(rolePo==null||rolePo.getBaserole()!=ISBASEROLE)
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
            PrivilegePo privilegePo=privDao.findPrivPo(vo.getPrivilegeId());
            if(privilegePo==null||privilegePo.getState()!=ISFORBIDEN)
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
            rolePrivilegePoMapper.insertSelective(po);
            BaseRolePrivilegeRetVo retvo=(BaseRolePrivilegeRetVo) Common.cloneVo(po,BaseRolePrivilegeRetVo.class);
            retvo.setName(privilegePo.getName());
            retvo.setCreator(new AdminVo(vo.getCreatorId(),vo.getCreatorName(),0));
            retvo.getModifier().setSign(0);
            retvo.setSign(0);
            return new ReturnObject(retvo);

        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.PRIVILEGE_RELATION_EXIST);
        }

    }

    /**
     * @author: zhang yu
     * @date: 2021/11/25 20:11
     * @version: 1.0
     */
    /**
     * 由角色id,privilegeid删除角色对应权限
     * @param rid
     * @param pid
     * @return
     */

    public ReturnObject delBaseRolePriv(Long rid,Long pid){
        try {
            RolePrivilegePoExample example = new RolePrivilegePoExample();
            RolePrivilegePoExample.Criteria criteria = example.createCriteria();
            criteria.andRoleIdEqualTo(rid);
            criteria.andPrivilegeIdEqualTo(pid);
            String key=String.format(BASEROLEKEY,rid);
            redisUtil.del(key);
            int ret = rolePrivilegePoMapper.deleteByExample(example);
            return new ReturnObject(ReturnNo.OK);
        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }
    }
    public void delBaseRolePrivByid(Long id)
    {
        try
        {
            rolePrivilegePoMapper.deleteByPrimaryKey(id);
        }catch (Exception e)
        {
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

}
