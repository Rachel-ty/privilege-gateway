package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class GroupRole {
    private Long id;
    private Long roleId;
    private Long groupId;
    private String signature;
    private Long creatorId;
    private LocalDateTime gmtCreate;
    private Long modifierId;
    private LocalDateTime gmtModified;
    private String creatorName;
    private String modifierName;
}
