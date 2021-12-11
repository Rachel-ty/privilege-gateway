package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 权限返回VO
 * @author Ming Qiu
 * @date Created in 2020/11/3 23:34
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivilegeRetVo {

    private Long id;

    private String name;

    private String url;

    private Byte requestType;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime gmtCreate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "GMT+8")
    private LocalDateTime gmtModified;

    private UserSimpleRetVo creator;

    private UserSimpleRetVo modifier;

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
