package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;


import lombok.*;

import java.time.LocalDateTime;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupRelationVo {
    private Long id;
    private String name;
    private LocalDateTime gmtCreate;
    private LocalDateTime gmtModified;
    private SimpleAdminUser creator;
    private SimpleAdminUser modifier;
    private Byte sign;

}
