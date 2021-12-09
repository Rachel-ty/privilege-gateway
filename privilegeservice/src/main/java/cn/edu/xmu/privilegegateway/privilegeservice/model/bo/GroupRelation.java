package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class GroupRelation {

    private Long id;
    private Long groupPId;
    private Long groupSId;
    private String signature;
    private Long creatorId;
    private LocalDateTime gmtCreate;
    private Long modifierId;
    private LocalDateTime gmtModified;
    private Byte sign;
}
