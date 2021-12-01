package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限返回VO
 * @author Ming Qiu
 * @date Created in 2020/11/3 23:34
 **/
@Data
public class PrivilegeRetVo {

    private Long id;

    private String name;

    private String url;

    private Byte requestType;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    private AdminVo creator;

    private AdminVo modifier;

    private Integer sign;

    public PrivilegeRetVo(Privilege obj){
        this.id = obj.getId();
        this.name = obj.getName();
        this.url = obj.getUrl();
        this.requestType = obj.getRequestType().getCode();
        this.gmtCreate = obj.getGmtCreate();
        this.gmtModified = obj.getGmtModified();
    }
}
