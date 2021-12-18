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
import cn.edu.xmu.privilegegateway.privilegeservice.dao.PrivilegeDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeRedisVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeVo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
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

    public final static Byte FORBIDDEN = 1;
    public final static Byte NORMAL = 0;


    public ReturnObject getPrivilegeStates() {
        List<Map<String, Object>> stateList;
        stateList = new ArrayList<>();
        for (Privilege.PrivilegeType privilegeType : Privilege.PrivilegeType.values()) {
            Map<String, Object> temp = new HashMap<>();
            temp.put("code", privilegeType.getCode());
            temp.put("name", privilegeType.getDescription());
            stateList.add(temp);
        }
        return new ReturnObject<>(stateList);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject<VoObject> AddPrivileges(PrivilegeVo vo, Long creatorId, String creatorName) {
        Privilege privilege = (Privilege) Common.cloneVo(vo, Privilege.class);
        Common.setPoCreatedFields(privilege, creatorId, creatorName);
        return privilegeDao.addPriv(privilege);
    }

    /**
     * 查询权限
     *
     * @param url
     * @param requestType
     * @param pagenum
     * @param pagesize
     * @return
     */
    @Transactional(readOnly = true, rollbackFor = Exception.class)
    public ReturnObject GetPriv(String url, Byte requestType, Integer pagenum, Integer pagesize) {
        return privilegeDao.getPriv(url, requestType, pagenum, pagesize);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject changePriv(Long privilegeId, PrivilegeVo vo, Long modifierId, String modifierName) {
        Privilege privilege = (Privilege) Common.cloneVo(vo, Privilege.class);
        privilege.setId(privilegeId);
        Common.setPoModifiedFields(privilege, modifierId, modifierName);
        privilege.setState(NORMAL);
        return privilegeDao.changePriv(privilege);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject DelPriv(Long privid) {
        return privilegeDao.delPriv(privid);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject ForbidPriv(Long privilegeId, Long modifierId, String modifierName) {
        Privilege privilege = new Privilege();
        privilege.setId(privilegeId);
        Common.setPoModifiedFields(privilege, modifierId, modifierName);
        privilege.setState(FORBIDDEN);
        return privilegeDao.changePrivState(privilege);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject ReleasePriv(Long privilegeId, Long modifierId, String modifierName) {
        Privilege privilege = new Privilege();
        privilege.setId(privilegeId);
        Common.setPoModifiedFields(privilege, modifierId, modifierName);
        privilege.setState(NORMAL);
        return privilegeDao.changePrivState(privilege);
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnObject loadPrivilege(PrivilegeRedisVo privilegeVo) {
        return privilegeDao.loadPrivilege(privilegeVo.getUrl(), privilegeVo.getRequestType());
    }
}
