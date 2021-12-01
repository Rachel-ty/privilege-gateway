package cn.edu.xmu.privilegegateway.privilegeservice.service;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.RoleDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.RoleInherited;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.RoleInheritedRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.RoleRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.StateVo;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色服务类
 *
 * @author 24320182203281 王纬策
 * createdBy 王纬策 2020/11/04 13:57
 * modifiedBy 王纬策 2020/11/7 19:20
 **/
@Service
public class RoleService {
    @Autowired
    RoleDao roleDao;

    @Autowired
    UserDao userDao;

    /**
     * 分页查询所有角色
     *
     * @author 24320182203281 王纬策
     * @param page  页数
     * @param pageSize 每页大小
     * @return ReturnObject<PageInfo < VoObject>> 分页返回角色信息
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2020/11/26 10:55
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectAllRoles(Long did, Integer page, Integer pageSize) {
        return roleDao.selectAllRole(did, page, pageSize);
    }

    /**
     * 新增角色
     *
     * @author 24320182203281 王纬策
     * @param bo 角色视图
     * @return ReturnObject<VoObject> 角色返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:03
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject insertRole(Role bo) {
        ReturnObject retObj = roleDao.insertRole(bo);

        return Common.getRetVo(retObj, RoleRetVo.class);
    }

    /**
     * 删除角色
     *
     * @author 24320182203281 王纬策
     * @param roleId 角色id
     * @return ReturnObject<Object> 返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:15
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject deleteRole(Long roleId, Long did) {
        return roleDao.deleteRole(roleId, did);
    }

    /**
     * 修改角色
     *
     * @author 24320182203281 王纬策
     * @param bo 角色视图
     * @return ReturnObject<Object> 角色返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:26
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject updateRole(Role bo) {
        return roleDao.updateRole(bo);
    }

    /**
     * 禁用角色
     *
     * @author 22920192204289 王文凯
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject forbidRole(Role bo) {
        return roleDao.updateRole(bo);
    }

    /**
     * 解禁角色
     *
     * @author 22920192204289 王文凯
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject releaseRole(Role bo) {
        return roleDao.updateRole(bo);
    }

    /**
     * 查询角色中用户
     *
     * @author 22920192204289 王文凯
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectUserByRole(Long roleId, Long did, Integer page, Integer pageSize) {
        return roleDao.selectUserByRole(roleId, did, page, pageSize);
    }

    @Transactional
    public void clearuserByroleId(Long id){
        userDao.clearUserByRoleId(id);
    }

    /**
     * 查询角色权限
     * @param id 角色id
     * @return 权限列表
     * createdBy wc 24320182203277
     */
    public ReturnObject<List> findRolePrivs(Long id){
        ReturnObject<List>  ret = roleDao.getRolePrivByRoleId(id);
        return ret;
    }


    /**
     * 取消角色权限
     * @param id 角色权限id
     * @return 权限列表
     * createdBy wc 24320182203277
     */
    @Transactional
    public ReturnObject<Object> delRolePriv(Long id){
        ReturnObject<Object> ret = roleDao.delPrivByPrivRoleId(id);
        //删除成功，缓存中干掉用户
        if(ret.getCode()==ReturnNo.OK) clearuserByroleId(id);
        return ret;
    }

    /**
     * 增加角色权限
     * @param roleid 角色id
     * @param privid 权限id
     * @param userid 用户id
     * @return 权限列表
     * createdBy 王琛 24320182203277
     */
    @Transactional
    public ReturnObject<VoObject> addRolePriv(Long roleid, Long privid, Long userid){
        //新增
        ReturnObject<VoObject> ret = roleDao.addPrivByRoleIdAndPrivId(roleid, privid, userid);
        //新增成功，缓存中干掉用户
        if(ret.getCode()==ReturnNo.OK) clearuserByroleId(roleid);
        return ret;
    }

    /**
     * 设置角色的继承关系
     * @author 张晖婧
     * @param pid 父角色id
     * @param cid 子角色id
     * @param createId 创建者id
     * @param did 部门id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<VoObject> createRoleInherited(Long createId,String createName,Long did, Long pid, Long cid) {
        RoleInherited roleInherited=new RoleInherited();
        Common.setPoCreatedFields(roleInherited,createId,createName);
        Common.setPoModifiedFields(roleInherited,createId,createName);
        roleInherited.setRoleId(pid);
        roleInherited.setRoleCId(cid);

        ReturnObject returnObject= roleDao.createRoleInherited(roleInherited,did);

        if (returnObject.getCode()!=ReturnNo.OK)
            return returnObject;
        RoleInherited roleInheritedBo=(RoleInherited) returnObject.getData();
        RoleInheritedRetVo retVo=(RoleInheritedRetVo) Common.cloneVo(roleInheritedBo, RoleInheritedRetVo.class);

        return new ReturnObject(retVo);
    }

    /**
     * 查询角色的功能角色
     * @author 22920192204320 张晖婧
     * @param did: 部门 id
     * @param id: 用户 id
     * @return Object
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject findBaserolesByRoleId(Long did, Long id, Integer page, Integer pageSize) {
        return roleDao.findBaserolesByRoleId(did, id, page, pageSize);
    }

    /**
     * 获得角色的所有状态
     * @author 22920192204320 张晖婧
     * @return Object
     */
    public ReturnObject getAllStates() {
        Role.State[] states = Role.State.class.getEnumConstants();
        List<StateVo> stateVos = new ArrayList<StateVo>();
        for (int i = 0; i < states.length; i++) {
            stateVos.add(new StateVo(states[i]));
        }
        return new ReturnObject(stateVos);
    }
    /**
     * 查询父角色
     * @author 张晖婧 22920192204320
     * @param did: 部门 id
     * @param id: 角色 id
     * @return Object
     */
    public ReturnObject findParentRoles(Long did, Long id, Integer page, Integer pageSize) {
        return roleDao.findParentRoles(did, id, page, pageSize);
    }
}
