package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: zhang yu
 * @date: 2021/11/24 16:38
 * @version: 1.0
*/
@Data
@NoArgsConstructor
public class RolePrivilegeVo {
    private  Long roleid;
    private  Long privilegeid;
    private  Long createdbyid;
    private  Long createdname;
}
