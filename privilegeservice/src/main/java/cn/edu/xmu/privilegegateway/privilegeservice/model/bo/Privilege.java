package cn.edu.xmu.privilegegateway.privilegeservice.model.bo;

import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
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
 **/
@Data
@NoArgsConstructor
public class Privilege implements VoObject{



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

        public static RequestType getTypeByCode(Integer code) {
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

    /**
     * 构造函数
     *
     * @param po 用PO构造
     */
    public Privilege(PrivilegePo po) {
        this.id = po.getId();
        this.name = po.getName();
        this.url = po.getUrl();
        this.signature = po.getSignature();
        this.gmtCreate = po.getGmtCreate();
        this.gmtModified = po.getGmtModified();
        this.requestType = RequestType.getTypeByCode(po.getRequestType().intValue());

        StringBuilder signature1 = Common.concatString("-", po.getUrl(), po.getRequestType().toString());
        this.key = String.format(PrivilegeDao.PRIVILEGEKEY,url,requestType.getCode());
        signature1.append("-");
        signature1.append(po.getId());
        this.cacuSignature = SHA256.getSHA256(signature1.toString());
    }

    /**
     * 对象未篡改
     * @return
     */
    public Boolean authetic() {
        return this.cacuSignature.equals(this.signature);
    }

    @Override
    public Object createVo() {
        return new PrivilegeRetVo(this);
    }

    @Override
    public Object createSimpleVo() {
        return new PrivilegeSimpleRetVo(this);
    }

    /**
     * 用vo对象创建更新po对象
     * @param vo vo对象
     * @return po对象
     */
    public PrivilegePo createUpdatePo(PrivilegeVo vo){
        PrivilegePo po = new PrivilegePo();
        po.setId(this.getId());
        po.setName(vo.getName());
        po.setUrl(vo.getUrl());
        po.setRequestType(vo.getRequestType().byteValue());
        po.setGmtCreate(null);
        po.setGmtModified(LocalDateTime.now());
        StringBuilder signature = Common.concatString("-", po.getUrl(), po.getRequestType().toString(), po.getId().toString());
        po.setSignature(SHA256.getSHA256(signature.toString()));
        return po;
    }
}
