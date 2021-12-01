package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author 张晖婧
 * @date 2021/11/26
 */
@Data
public class RoleInherited {
    private Long id;

    private String name;

    private Long roleId;

    private Long roleCId;

    private String Signature;

    private Long creatorId;

    private String creatorName;

    private Long modifierId;

    private String modifierName;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    private Byte sign;
}
