package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserRolePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.UserRoleRetVo;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.encript.SHA256;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class UserRole implements VoObject {
    private Long id;

    private String name;

    private Long userId;

    private Long roleId;

    private Long creatorId;

    private String creatorName;

    private LocalDateTime gmtCreate;

    private Long modifierId;

    private String modifierName;

    private LocalDateTime gmtModified;

    private Byte sign;

    private String signature;

    private String cacuSignature;

    public UserRole(UserRolePo userRolePo, User user, Role role, User creator) {
        this.id = userRolePo.getId();
        this.gmtCreate = userRolePo.getGmtCreate();
        this.signature = userRolePo.getSignature();

        StringBuilder signature = Common.concatString("-",
                userRolePo.getUserId().toString(), userRolePo.getRoleId().toString(), userRolePo.getCreatorId().toString());
        this.cacuSignature = SHA256.getSHA256(signature.toString());
    }

    /**
     * 对象未篡改
     * @return
     */
    public Boolean authetic() {
        return this.cacuSignature.equals(this.signature);
    }

    @Override
    public Object createVo() {
        UserRoleRetVo userRoleRetVo = new UserRoleRetVo();
        userRoleRetVo.setId(this.id);
        userRoleRetVo.setGmtCreate(this.gmtCreate.toString());

        return userRoleRetVo;
    }

    @Override
    public Object createSimpleVo() {
        return null;
    }
}
