package cn.edu.xmu.privilegegateway.privilegeservice.service;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.PrivilegeDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.RoleDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.PrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.BasePrivilegeRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeVo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: zhang yu
 * @date: 2021/11/22 9:03
 * @version: 1.0
*/
@Service
public class PrivilegeService {
    @Autowired
    PrivilegeDao privilegeDao;
    public ReturnObject getPrivilegeStates()
    {
        List<Map<String,Object>> stateList;
        stateList = new ArrayList<>();
        for(Privilege.RequestType requestType:Privilege.RequestType.values()){
            Map<String,Object> temp=new HashMap<>();
            temp.put("code",requestType.getCode());
            temp.put("name",requestType.getDescription());
            stateList.add(temp);
        }
        return new ReturnObject<>(stateList);
    }

    /**
     *
     * @param vo
     * @param cid
     * @return
     */
    public ReturnObject<VoObject> AddPrivileges(PrivilegeVo vo, Long cid,String cname)
    {
        return privilegeDao.addPriv(vo,cid,cname);
    }

    /**
     *查询权限
     * @param url
     * @param requestType
     * @param pagenum
     * @param pagesize
     * @return
     */
    @Transactional(readOnly = true)
    public ReturnObject<PageInfo<BasePrivilegeRetVo>> GetPriv(String url , Byte requestType, Integer pagenum, Integer pagesize)
    {
        return privilegeDao.getPriv(url,requestType,pagenum,pagesize);
    }
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject DelPriv(Long privid)
    {
        return privilegeDao.delPriv(privid);
    }
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject ForbidPriv(Long privid)
    {
        return privilegeDao.forbidPriv(privid);
    }
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject ReleasePriv(Long pid)
    {
        return privilegeDao.releasePriv(pid);
    }
}
