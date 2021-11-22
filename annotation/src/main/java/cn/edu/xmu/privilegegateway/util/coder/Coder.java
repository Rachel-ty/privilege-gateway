package cn.edu.xmu.privilegegateway.util.coder;

import cn.edu.xmu.privilegegateway.util.Common;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Coder {
    protected Object originObj;
    protected Sign sign;
    protected List<String> fieldNameList;
    private boolean needSign;

    public Coder(Object originObj, Sign sign, String... fieldNames) {
        this.originObj = originObj;
        this.sign = sign;
        fieldNameList = new ArrayList<>();
        fieldNameList.addAll(Arrays.asList(fieldNames));
        needSign = true;
    }

    public Coder(Object originObj, String... fieldNames) {
        this.originObj = originObj;
        fieldNameList = new ArrayList<>();
        fieldNameList.addAll(Arrays.asList(fieldNames));
        needSign = false;
    }

    public abstract String encrypt(String content);

    public abstract String decrypt(String content);

    public Object code(Class poClass) {
        String signature = null;
        if (needSign) {
            signature = sign.getSignature(originObj);
        }

        // 字段加密
        for(String fieldName : fieldNameList) {
            try {
                Field field = originObj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                String originValue = (String) field.get(originObj);
                String encryptValue = encrypt(originValue);
                field.set(originObj, encryptValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                continue;
            }
        }

        if (needSign) {
            // 生成签名
            try {
                Field signatureField = originObj.getClass().getDeclaredField("signature");
                signatureField.setAccessible(true);
                signatureField.set(originObj, signature);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return Common.cloneVo(originObj, poClass);
    }

    /**
     *
     * @param boClass
     * @return null代表信息已被篡改
     */
    public Object decode(Class boClass) {
        // 字段解密
        for(String fieldName : fieldNameList) {
            try {
                Field field = originObj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                String originValue = (String) field.get(originObj);
                String decryptValue = decrypt(originValue);
                field.set(originObj, decryptValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                continue;
            }
        }

        // 校验签名
        if (!sign.check(originObj)) {
            return null;
        }

        Object bo = Common.cloneVo(originObj, boClass);

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
