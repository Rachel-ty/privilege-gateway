package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.annotation.util.encript.SHA256;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.PrivilegeDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.PrivilegePo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeSimpleRetVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.PrivilegeVo;
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
