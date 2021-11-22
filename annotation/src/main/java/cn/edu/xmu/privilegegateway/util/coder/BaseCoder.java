package cn.edu.xmu.privilegegateway.util.coder;

import cn.edu.xmu.privilegegateway.util.Common;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author wang zhongyu
 * @date 2021-11-22
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseCoder {
    protected BaseSign sign;
    protected List<String> fieldNameList;

    public BaseCoder(BaseSign sign, String... fieldNames) {
        this.sign = sign;
        fieldNameList = new ArrayList<>();
        fieldNameList.addAll(Arrays.asList(fieldNames));
    }

    /**
     * 具体的加密算法
     * @param content
     * @return
     */
    public abstract String encrypt(String content);

    /**
     * 具体的解密算法
     * @param content
     * @return
     */
    public abstract String decrypt(String content);

    public Object code(Object originObj, Class poClass) {
        String signature = sign.getSignature(originObj);

        Object po = Common.cloneVo(originObj, poClass);

        // 字段加密
        for(String fieldName : fieldNameList) {
            try {
                Field field = po.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                String originValue = (String) field.get(po);
                String encryptValue = encrypt(originValue);
                field.set(po, encryptValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                continue;
            }
        }

        // 生成签名
        try {
            Field signatureField = po.getClass().getDeclaredField("signature");
            signatureField.setAccessible(true);
            signatureField.set(po, signature);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return po;
    }

    /**
     *
     * @param boClass
     * @return null代表信息已被篡改
     */
    public Object decode(Object originObj, Class boClass) {
        Object bo = Common.cloneVo(originObj, boClass);

        // 字段解密
        for(String fieldName : fieldNameList) {
            try {
                Field field = bo.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                String originValue = (String) field.get(bo);
                String decryptValue = decrypt(originValue);
                field.set(bo, decryptValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                continue;
            }
        }

        // 校验签名
        if (!sign.check(bo)) {
            return null;
        }


        // 将签名写入cacuSignature字段
        try {
            Field signatureField = bo.getClass().getDeclaredField("signature");
            signatureField.setAccessible(true);
            Object signature = signatureField.get(bo);

            Field cacuSignatureField = bo.getClass().getDeclaredField("cacuSignature");
            cacuSignatureField.setAccessible(true);
            cacuSignatureField.set(bo, signature);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return bo;
    }
}
