package cn.edu.xmu.privilegegateway.annotation.model;

import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.encript.AES;
import cn.edu.xmu.privilegegateway.annotation.util.encript.SHA256;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台用户
 *
 * @author Ming Qiu
 * @date Created in 2020/11/3 20:10
 * Modified at 2020/11/4 21:23
 **/
@Data
@NoArgsConstructor
public class User {

    /**
     * 后台用户状态
     */
    public enum State {
        NEW(0, "新注册"),
        NORM(1, "正常"),
        FORBID(2, "封禁"),
        DELETE(3, "废弃");

        private static final Map<Integer, State> stateMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
            stateMap = new HashMap();
            for (State enum1 : values()) {
                stateMap.put(enum1.code, enum1);
            }
        }

        private int code;
        private String description;

        State(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static State getTypeByCode(Integer code) {
            return stateMap.get(code);
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

    private Long id;

    private String userName;

    private String password;

    private String mobile;

    private Boolean mobileVerified = false;

    private String email;

    private Boolean emailVerified = false;

    private String name;

    private String avatar;

    private Integer level;

    private LocalDateTime lastLoginTime;

    private String lastLoginIp;

    private String openId;

    private State state = State.NEW;

    private Long departId;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    private Long creatorId;

    private String signature;

    private String cacuSignature;
}
