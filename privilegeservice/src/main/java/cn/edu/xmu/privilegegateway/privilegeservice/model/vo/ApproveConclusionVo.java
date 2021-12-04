package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ApproveConclusionVo {
    @ApiModelProperty(value = "审核结果")
    private boolean approve;
    public boolean getApprove(){
        return this.approve;
    }
}
