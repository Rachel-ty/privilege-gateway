package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.PrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.RolePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.RolePrivilegeRetVo;
import lombok.Data;

/**
 * 角色权限
 * @author wc 24320182203277
 * @date
 **/

@Data
public class RolePrivilege implements VoObject {
    private Long id= null;
    private RolePo role = new RolePo();
    private PrivilegePo privilege = new PrivilegePo();
    private UserPo creator = new UserPo();
    private String gmtModified = null;
    @Override
    public RolePrivilegeRetVo createVo() {
        return new RolePrivilegeRetVo(this);
    }

    @Override
    public Object createSimpleVo() {
        return new RolePrivilegeRetVo(this);
    }
}
