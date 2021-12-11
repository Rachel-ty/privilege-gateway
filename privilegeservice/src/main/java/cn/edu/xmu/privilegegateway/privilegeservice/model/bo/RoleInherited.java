package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 张晖婧
 * @date 2021/11/26
 */
@Data
@NoArgsConstructor
public class RoleInherited {
    private Long id;

    private String name;

    private Long roleId;

    private Long roleCId;

    private String signature;

    private Long creatorId;

    private String creatorName;

    private Long modifierId;

    private String modifierName;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    private Byte sign;
}
