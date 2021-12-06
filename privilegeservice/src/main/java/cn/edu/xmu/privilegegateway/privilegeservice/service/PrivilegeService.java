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
import cn.edu.xmu.privilegegateway.annotation.util.InternalReturnObject;
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

    public final static Byte FORBIDEN=1;
    public final static Byte NORMAL=0;
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
    public ReturnObject<VoObject> AddPrivileges(PrivilegeVo vo, Long creatorId,String creatorName)
    {
        Privilege privilege=(Privilege) Common.cloneVo(vo,Privilege.class);
        Common.setPoCreatedFields(privilege,creatorId,creatorName);
        return privilegeDao.addPriv(privilege);
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
    public ReturnObject changePriv(Long privilegeId,PrivilegeVo vo,Long modifierId,String modifierName)
    {
        Privilege privilege = (Privilege) Common.cloneVo(vo,Privilege.class);
        privilege.setId(privilegeId);
        Common.setPoModifiedFields(privilege,modifierId,modifierName);
        privilege.setState(NORMAL);
        return privilegeDao.changePriv(privilege);
    }
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject DelPriv(Long privid)
    {
        return privilegeDao.delPriv(privid);
    }
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject ForbidPriv(Long privilegeId,Long modifierId,String modifierName)
    {
        Privilege privilege=new Privilege();
        privilege.setId(privilegeId);
        Common.setPoModifiedFields(privilege,modifierId,modifierName);
        privilege.setState(FORBIDEN);
        return privilegeDao.changePriv(privilege);
    }
    @Transactional(rollbackFor = Exception.class)
    public ReturnObject ReleasePriv(Long privilegeId,Long modifierId,String modifierName)
    {
        Privilege privilege=new Privilege();
        privilege.setId(privilegeId);
        Common.setPoModifiedFields(privilege,modifierId,modifierName);
        privilege.setState(NORMAL);
        return privilegeDao.changePriv(privilege);
    }
    @Transactional(rollbackFor = Exception.class)
    public InternalReturnObject loadPrivilege(PrivilegeVo privilegeVo) {
        return privilegeDao.loadPrivilege(privilegeVo.getUrl(),privilegeVo.getRequestType());
    }
}
