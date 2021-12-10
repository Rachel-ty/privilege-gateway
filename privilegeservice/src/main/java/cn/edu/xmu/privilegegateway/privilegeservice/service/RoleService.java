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
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.PrivilegeDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.RoleDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @author 24320182203281 王纬策
     * @param pageNum  页数
     * @param pageSize 每页大小
     * @return ReturnObject<PageInfo < VoObject>> 分页返回角色信息
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     */
    public ReturnObject<PageInfo<VoObject>> selectAllRoles(Long departId, Integer pageNum, Integer pageSize) {
        ReturnObject<PageInfo<VoObject>> returnObject = roleDao.selectAllRole(departId, pageNum, pageSize);
        return returnObject;
    }

    /**
     * 新增角色
     * @author 24320182203281 王纬策
     * @param role 角色视图
     * @return ReturnObject<VoObject> 角色返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     */
    @Transactional
    public ReturnObject insertRole(Role role) {
        ReturnObject<Role> retObj = roleDao.insertRole(role);
        return retObj;
    }

    /**
     * 删除角色
     * @author 24320182203281 王纬策
     * @param id 角色id
     * @return ReturnObject<Object> 返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     */
    @Transactional
    public ReturnObject<Object> deleteRole(Long did, Long id) {
        return roleDao.deleteRole(did, id);
    }

    /**
     * 修改角色
     * @author 24320182203281 王纬策
     * @param bo 角色视图
     * @return ReturnObject<Object> 角色返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     */
    @Transactional
    public ReturnObject updateRole(Role bo) {
        ReturnObject<Role> retObj = roleDao.updateRole(bo);
        return retObj;
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
     * author:zhangyu
     * 查询功能角色权限
     * @param roleid
     * @param pagenum
     * @param pagesize
     * @return
     */
    @Transactional(readOnly = true)
    public ReturnObject selectBaseRolePrivs(Long roleid, Integer pagenum, Integer pagesize)
    {
        if(roleDao.isBaseRole(roleid))
        {
            return privilegeDao.selectBaseRolePrivs(roleid,pagenum,pagesize);
        }
        else
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
    }

    /**
     * author:zhangyu
     * 删除角色对应的权限（功能角色用）
     * @param rid
     * @param pid
     * @return
     */
    @Transactional
    public ReturnObject<Object> delBaseRolePriv(Long rid,Long pid){
        if(roleDao.isBaseRole(rid))
        {
            ReturnObject returnObject=privilegeDao.delRolePriv(rid,pid);
            return  returnObject;
        }
        return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
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
//        if(ret.getCode()==ReturnNo.OK) clearuserByroleId(id);
        return ret;
    }

    /**
     * 增加功能角色权限
     * @return 权限列表
     * createdBy 王琛 24320182203277
     * modifiedby zhangyu
     */
    /**
     * 获取功能角色权限
     * @param roleid
     * @param privilegeid
     * @param creatorid
     * @param creatorname
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject addBaseRolePriv(Long roleid,Long privilegeid,Long creatorid,String creatorname){
        //新增
        if(roleDao.isBaseRole(roleid))
        {
            ReturnObject returnObject=privilegeDao.addBaseRolePriv(roleid,privilegeid,creatorid,creatorname);
            return returnObject;
        }

        else
            return new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE);
    }

}
