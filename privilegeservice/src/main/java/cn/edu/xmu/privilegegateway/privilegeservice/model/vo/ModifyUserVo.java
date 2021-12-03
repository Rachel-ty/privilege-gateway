package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 用户信息 Vo
 * @author 蒋欣雨
 * Created at 2021/12/1
 **/
@Data
@ApiModel(description = "用户修改信息视图对象")
public class ModifyUserVo {

    @ApiModelProperty(value = "用户姓名")
    private String name;

    @ApiModelProperty(value = "用户头像 URL")
    private String avatar;

    @ApiModelProperty(value = "用户身份证号")
    private String idNumber;
    @ApiModelProperty(value = "护照号")
    private String passportNumber;
    @ApiModelProperty(value = "用户等级")
    private Integer level;

}
