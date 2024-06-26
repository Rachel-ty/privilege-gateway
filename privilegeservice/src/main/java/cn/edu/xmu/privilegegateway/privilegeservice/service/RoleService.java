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
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.PrivilegeDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.RoleDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.RoleInherited;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.RoleInheritedRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.RoleRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.StateVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
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

    @Autowired
    PrivilegeDao privilegeDao;

    @Autowired
    RedisUtil redisUtil;

    /**
     * 分页查询所有角色
     *
     * @param page     页数
     * @param pageSize 每页大小
     * @return ReturnObject<PageInfo < VoObject>> 分页返回角色信息
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2020/11/26 10:55
     * @author 24320182203281 王纬策
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectAllRoles(Long did, Integer page, Integer pageSize) {
        return roleDao.selectAllRole(did, page, pageSize);
    }


    /**
     * 查询功能角色
     *
     * @author 22920192204289 王文凯
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectBaseRoles(Integer page, Integer pageSize) {
        return roleDao.selectBaseRole(page, pageSize);
    }

    /**
     * 新增角色
     *
     * @param bo 角色视图
     * @return ReturnObject<VoObject> 角色返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:03
     * @author 24320182203281 王纬策
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject insertRole(Role bo) {
        ReturnObject retObj = roleDao.insertRole(bo);

        return Common.getRetVo(retObj, RoleRetVo.class);
    }

    /**
     * 新增功能角色
     *
     * @param bo 角色视图
     * @return ReturnObject<VoObject> 角色返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:03
     * @author 24320182203281 王纬策
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject insertBaseRole(Role bo) {
        ReturnObject retObj = roleDao.insertRole(bo);

        return Common.getRetVo(retObj, RoleRetVo.class);
    }

    /**
     * 删除角色
     *
     * @param roleId 角色id
     * @return ReturnObject<Object> 返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:15
     * @author 24320182203281 王纬策
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject deleteRole(Long roleId, Long did) {
        return roleDao.deleteRole(roleId, did);
    }

    /**
     * 修改角色
     *
     * @param bo 角色视图
     * @return ReturnObject<Object> 角色返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:26
     * @author 24320182203281 王纬策
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject updateRole(Role bo) {
        RolePo rolePo=roleDao.getRolePoByRoleId(bo.getId());
        if(rolePo==null)
        {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
        return roleDao.updateRole(bo);
    }

    /**
     * 禁用角色
     *
     * @author 22920192204289 王文凯
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject forbidRole(Role bo) {
        RolePo ret=roleDao.getRolePoByRoleId(bo.getId());
        if(ret==null)
        {
            return new ReturnObject(ReturnNo.RESOURCE_ID_NOTEXIST);
        }
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


    /**
     * author:zhangyu
     * 查询功能角色权限
     *
     * @param roleid
     * @param pagenum
     * @param pagesize
     * @return
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject selectBaseRolePrivs(Long roleid, Integer pagenum, Integer pagesize) {
        ReturnObject baseRole = roleDao.isBaseRole(roleid);
        if (baseRole.getCode() != ReturnNo.OK) {
            return baseRole;
        }
        return privilegeDao.selectBaseRolePrivs(roleid, pagenum, pagesize);
    }

    /**
     * author:zhangyu
     * 删除角色对应的权限（功能角色用）
     *
     * @param rid
     * @param pid
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<Object> delBaseRolePriv(Long rid, Long pid) {
        ReturnObject baseRole = roleDao.isBaseRole(rid);
        if (baseRole.getCode() != ReturnNo.OK) {
            return baseRole;
        }
        ReturnObject returnObject = privilegeDao.delRolePriv(rid, pid);
        return returnObject;
    }

    /**
     * 增加功能角色权限
     * @return 权限列表
     * createdBy 王琛 24320182203277
     * modifiedby zhangyu
     */
    /**
     * 获取功能角色权限
     *
     * @param roleid
     * @param privilegeid
     * @param creatorid
     * @param creatorname
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject addBaseRolePriv(Long roleid, Long privilegeid, Long creatorid, String creatorname) {
        //新增
        ReturnObject baseRole = roleDao.isBaseRole(roleid);
        if (baseRole.getCode() != ReturnNo.OK) {
            return baseRole;
        }
        ReturnObject returnObject = privilegeDao.addBaseRolePriv(roleid, privilegeid, creatorid, creatorname);
        return returnObject;
    }

    /**
     * 设置角色的继承关系
     *
     * @param pid      父角色id
     * @param cid      子角色id
     * @param createId 创建者id
     * @param did      部门id
     * @return
     * @author 张晖婧
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<VoObject> createRoleInherited(Long createId, String createName, Long did, Long pid, Long cid) {
        RoleInherited roleInherited = new RoleInherited();
        Common.setPoCreatedFields(roleInherited, createId, createName);
        Common.setPoModifiedFields(roleInherited, createId, createName);
        roleInherited.setRoleId(pid);
        roleInherited.setRoleCId(cid);

        ReturnObject returnObject = roleDao.createRoleInherited(roleInherited, did);

        if (returnObject.getCode() != ReturnNo.OK)
            return returnObject;
        RoleInherited roleInheritedBo = (RoleInherited) returnObject.getData();
        RoleInheritedRetVo retVo = (RoleInheritedRetVo) Common.cloneVo(roleInheritedBo, RoleInheritedRetVo.class);

        return new ReturnObject(retVo);
    }

    /**
     * 查询角色的功能角色
     *
     * @param did: 部门 id
     * @param id:  用户 id
     * @return Object
     * @author 22920192204320 张晖婧
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject findBaserolesByRoleId(Long did, Long id, Integer page, Integer pageSize) {
        return roleDao.findBaserolesByRoleId(did, id, page, pageSize);
    }

    /**
     * 获得角色的所有状态
     *
     * @return Object
     * @author 22920192204320 张晖婧
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
     *
     * @param did: 部门 id
     * @param id:  角色 id
     * @return Object
     * @author 张晖婧 22920192204320
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject findParentRoles(Long did, Long id, Integer page, Integer pageSize) {
        return roleDao.findParentRoles(did, id, page, pageSize);
    }
}
