package cn.edu.xmu.privilegegateway.privilegeservice.dao;

import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.PrivilegePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.RolePoMapper;
import cn.edu.xmu.privilegegateway.privilegeservice.mapper.RolePrivilegePoMapper;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
        RolePo rolePo=null;
        try{
            rolePo=roleMapper.selectByPrimaryKey(rid);
            if(rolePo==null||rolePo.getBaserole()!=0)
            {
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
            }


        }catch (Exception e)
        {
            return new ReturnObject(ReturnNo.INTERNAL_SERVER_ERR);
        }

        RolePrivilegePoExample example=new RolePrivilegePoExample();
        RolePrivilegePoExample.Criteria criteria=example.createCriteria();
        criteria.andRoleIdEqualTo(rid);
        try
        {
            PageHelper.startPage(pagenum,pagesize);
            List<RolePrivilegePo> polist=rolePrivilegePoMapper.selectByExample(example);
            List<BasePrivilegeRetVo> voList=new ArrayList<BasePrivilegeRetVo>();
            for(RolePrivilegePo po:polist)
            {
                PrivilegePo privilegePo=privDao.findPrivByid(po.getPrivilegeId());
                if(privilegePo!=null)
                {
                    int sign=0;
                    //校验权限是否错误
                    PrivilegePo newprivilegepo=(PrivilegePo) coder.decode_check(privilegePo,PrivilegePo.class,PrivilegeDao.codeFields,PrivilegeDao.signFields,"signatrue");
                    if(newprivilegepo==null)
                        sign=1;
                    //校验关系是否有错误
                    RolePrivilegePo newpo=(RolePrivilegePo)coder.decode_check(po,RolePrivilegePo.class,RolePrivilegeDao.codeFields,RolePrivilegeDao.signFields,"signature");
                    if(newpo==null)
                        sign=1;
                    BasePrivilegeRetVo vo=(BasePrivilegeRetVo) Common.cloneVo(newpo,BasePrivilegeRetVo.class);
                    vo.setCreator(new AdminVo(privilegePo.getCreatorId(),privilegePo.getCreatorName(),sign));
                    vo.setModifier(new AdminVo(privilegePo.getModifierId(), po.getModifierName(), sign));
                    vo.setSign(sign);
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
            if(rolePo==null||rolePo.getBaserole()!=0)
                return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
            PrivilegePo privilegePo=privilegePoMapper.selectByPrimaryKey(vo.getPrivilegeId());
            if(privilegePo==null)
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
     * 由权限id,privilegeid删除角色对应权限
     * @param rid
     * @param pid
     * @return
     */

    public ReturnObject delBaseRolePriv(Long rid,Long pid){
        ReturnObject retObj = null;
        RolePrivilegePoExample example=new RolePrivilegePoExample();
        RolePrivilegePoExample.Criteria criteria=example.createCriteria();
        criteria.andRoleIdEqualTo(rid);
        criteria.andPrivilegeIdEqualTo(pid);
        int ret = rolePrivilegePoMapper.deleteByExample(example);
        if(ret==0){
            retObj = new ReturnObject<>(ReturnNo.RESOURCE_ID_NOTEXIST);
        }else{
            retObj = new ReturnObject<>(ReturnNo.OK);
        }

        return retObj;
    }

}
