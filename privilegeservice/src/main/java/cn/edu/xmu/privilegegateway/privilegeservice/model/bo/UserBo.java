/**
 * Copyright School of Informatics Xiamen University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author BingShuai Liu
 * @studentId 22920192204245
 * @date 2021/11/29/4:05
 */
@Data
public class UserBo {

    public enum State {
        NORM(0, "正常"),
        FORBID(1, "禁止访问");

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
    private Byte mobileVerified;
    private String email;
    private Byte emailVerified;
    private String name;
    private String avatar;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private String openId;
    private Byte state;
    private Long departId;
    private String signature;
    private Long creatorId;
    private LocalDateTime gmtCreate;
    private Long modifierId;
    private LocalDateTime gmtModified;
    private String idNumber;
    private String passportNumber;
    private Integer level;
    private String creatorName;
    private String modifierName;
}
