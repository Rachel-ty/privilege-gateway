package cn.edu.xmu.privilegegateway.annotation.model.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Shop {
    public enum State {
        EXAME(0,"未审核"),
        OFFLINE(1,"下线"),
        ONLINE(2, "上线"),
        FORBID(3, "关闭");

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
    private String name;
    private Long deposit;
    private Long depositThreshold;
    private State state;
    private Long createdBy;
    private String createName;
    private Long modifiedBy;
    private String modiName;
    private LocalDateTime gmtCreated;
    private LocalDateTime gmtModified;
}

