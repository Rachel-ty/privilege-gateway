package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户id与用户名
 * @author Xianwei Wang
 **/
@Data
@ApiModel
public class UserSimpleRetVo {
    @ApiModelProperty(name = "用户id", value = "id")
    private Long id;

    @ApiModelProperty(name = "用户名", value = "userName")
    private String userName;

    public UserSimpleRetVo() {
    }
}