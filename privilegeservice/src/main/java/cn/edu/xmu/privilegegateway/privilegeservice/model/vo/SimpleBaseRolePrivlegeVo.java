package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleBaseRolePrivlegeVo {
    private Long roleId;
    private Long privilegeId;
    private Long creatorId;
    private String creatorName;
}
