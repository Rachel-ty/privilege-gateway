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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ming Qiu
 * @date Created in 2020/11/3 11:48
 * ModifiedBy Ming Qiu 2021-12-12 7:07
 **/
@Data
@NoArgsConstructor
public class Privilege{
    public enum PrivilegeType {
        NORMAL((byte)0, "NORMAL"),
        FORBIDDEN((byte)1, "FORBIDEN");


        private static final Map<Byte, PrivilegeType> typeMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
            typeMap = new HashMap();
            for (PrivilegeType enum1 : values()) {
                typeMap.put(enum1.code, enum1);
            }
        }

        private Byte code;
        private String description;

        PrivilegeType(Byte code, String description) {
            this.code = code;
            this.description = description;
        }

        public static PrivilegeType getTypeByCode(Byte code) {
            return typeMap.get(code);
        }

        public Byte getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

    }


    /**
     * 请求类型
     */
    public enum RequestType {
        GET((byte)0, "GET"),
        POST((byte)1, "POST"),
        PUT((byte)2, "PUT"),
        DELETE((byte)3, "DELETE");

        private static final Map<Byte, RequestType> typeMap;

        static { //由类加载机制，静态块初始加载对应的枚举属性到map中，而不用每次取属性时，遍历一次所有枚举值
            typeMap = new HashMap();
            for (RequestType enum1 : values()) {
                typeMap.put(enum1.code, enum1);
            }
        }

        private Byte code;
        private String description;

        RequestType(Byte code, String description) {
            this.code = code;
            this.description = description;
        }

        public static RequestType getTypeByCode(Byte code) {
            return typeMap.get(code);
        }

        public Byte getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

    }

    private Long id;

    private String name;

    private String url;

    private RequestType requestType;

    private String signature;

    private LocalDateTime gmtCreate;

    private LocalDateTime gmtModified;

    /**
     * privilege的key
     */
    private String key;

    /**
     * 计算出的签名
     */
    private String cacuSignature;

    private Integer sign;

    private Long creatorId;

    private Long modifierId;

    private String creatorName;

    private String modifierName;

    private Byte state;

}
