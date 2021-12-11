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
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 角色Bo类
 *
 * @author 24320182203281 王纬策
 * createdBy 王纬策 2020/11/04 13:57
 * modifiedBy 王纬策 2020/11/7 19:20
 * modifiedBy 王文凯 2021/11/26 17:02
 * modifiedBy 张晖婧 2021/11/30 22:41
 * ModifiedBy Ming Qiu 2021-12-12 7:07
 **/
@Data
@NoArgsConstructor
public class Role implements Serializable {
    private Long id;

    private String name;

    private String descr;

    private Long departId;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    private Long creatorId;

    private String creatorName;

    private String creatorSign;

    private Long modifierId;

    private String modifierName;

    private String modifierSign;

    private Byte state = State.NORM.getCode();

    private Byte baserole;

    private Byte sign;

    /**
     * @author 张晖婧
     * @date 2021/11/26
     **/
    public enum State {
        /**
         * NORM 正常状态
         * FORBID 禁用状态
         */
        NORM((byte)0, "正常"),
        FORBID((byte)1, "禁用");

        private static final Map<Byte, State> stateMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
            stateMap = new HashMap();
            for (Role.State enum1 : values()) {
                stateMap.put(enum1.code, enum1);
            }
        }

        private Byte code;
        private String description;

        State(Byte code, String description) {
            this.code = code;
            this.description = description;
        }

        public Byte getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }

}
