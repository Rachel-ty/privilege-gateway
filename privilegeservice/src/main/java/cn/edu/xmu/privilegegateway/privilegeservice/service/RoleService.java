package cn.edu.xmu.privilegegateway.privilegeservice.service;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.RoleDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.BasePrivilegeRetVo;
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

    @Transactional
    public void clearuserByroleId(Long id){
        userDao.clearUserByRoleId(id);
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
    public ReturnObject<PageInfo<BasePrivilegeRetVo>> selectBaseRolePrivs(Long roleid, Integer pagenum, Integer pagesize)
    {
        return roleDao.selectBaseRolePrivs(roleid,pagenum,pagesize);

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
     * 删除角色对应的权限（功能角色用）
     * @param rid
     * @param pid
     * @return
     */
    @Transactional
    public ReturnObject<Object> delBaseRolePriv(Long rid,Long pid){
        ReturnObject<Object> ret = roleDao.delBaseRolePriv(rid,pid);
        return ret;
    }

    /**
     * 增加功能角色权限
     * @return 权限列表
     * createdBy 王琛 24320182203277
     * modifiedby zhangyu
     */
    /**
     *
     * @param vo
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject addBaseRolePriv(Long roleid,Long privilegeid,Long creatorid,String creatorname){
        //新增
        ReturnObject ret = roleDao.addBaseRolePriv(roleid,privilegeid,creatorid,creatorname);
        return ret;
    }

}
