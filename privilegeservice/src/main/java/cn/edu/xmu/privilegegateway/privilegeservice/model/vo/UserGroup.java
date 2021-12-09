package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class UserGroup {
    private Long id;
    private Long userId;
    private Long groupId;
    private Long creatorId;
    private LocalDateTime gmtCreate;
    private Long modifierId;
    private LocalDateTime gmtModified;
    private Byte sign;
}
