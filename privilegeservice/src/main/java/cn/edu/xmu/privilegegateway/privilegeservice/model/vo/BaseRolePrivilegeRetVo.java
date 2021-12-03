package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseRolePrivilegeRetVo {
    private Long id;

    private String name;
    @JsonFormat(pattern = "yyyy-MM-ddTHH:mm:ss.SSSZ", timezone = "GMT+8")
    private LocalDateTime gmtCreate;

    @JsonFormat(pattern = "yyyy-MM-ddTHH:mm:ss.SSSZ", timezone = "GMT+8")
    private LocalDateTime gmtModified;

    private AdminVo creator;

    private AdminVo modifier;

    private Integer sign;
}
