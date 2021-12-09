package cn.edu.xmu.privilegegateway.privilegeservice.model.vo;

import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.User;
import lombok.Data;

/**
 * 管理员状态VO
 * @author LiangJi3229
 * @date 2020/11/10 18:41
 */
@Data
public class StateVo {
    private Long Code;

    private String name;

    public StateVo(User.State state) {
        Code = Long.valueOf(state.getCode());
        name = state.getDescription();
    }

    public StateVo(Role.State state) {
        Code = Long.valueOf(state.getCode());
        name = state.getDescription();
    }
}
