package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户id与用户名
 * @author Xianwei Wang
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("用户信息视图")
public class UserSimpleRetVo {
    @ApiModelProperty(name = "用户id", value = "id")
    private Long id;

    @ApiModelProperty(name = "用户名", value = "name")
    private String name;
}