package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "重置密码对象")
public class ResetPwdVo {
    @NotBlank(message = "不能为空")
    private String name;
}

