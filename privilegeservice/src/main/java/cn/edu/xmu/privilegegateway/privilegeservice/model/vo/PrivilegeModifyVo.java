package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author:李智樑
 * @time:2021/12/21 1:11
 **/
@Data
@NoArgsConstructor
@ApiModel("权限修改传值对象")
public class PrivilegeModifyVo {

    private String name;

    private String url;

    @Range(min = 0, max = 3, message = "错误的requestType数值")
    private Byte requestType;
}
