package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.SimpleAdminUser;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel
public class UserRelationBo {
    private Long id;
    private String name;
    private String gmtCreate;
    private String gmtModified;
    private SimpleAdminUser creator;
    private SimpleAdminUser modifier;
    private Byte sign;
    private String signature;
}
