package cn.edu.xmu.privilegegateway.util.coder;

import cn.edu.xmu.privilegegateway.util.Common;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author wang zhongyu
 * @date 2021-11-22
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseSign {
    private Logger logger = LoggerFactory.getLogger(BaseSign.class);

    protected String signatureFieldName;

    protected List<String> fieldNameList;

    public BaseSign(String signatureFieldName, String... fieldNames) {
        this.signatureFieldName = signatureFieldName;
        fieldNameList = new ArrayList<>();
        fieldNameList.addAll(Arrays.asList(fieldNames));
    }

    /**
     * 具体的加密算法
     * @param content
     * @return
     */
    protected abstract String encrypt(String content);

    public boolean check(Object obj) {
        String cacuSignature = getSignature(obj);

        try {
            Field signatureField = obj.getClass().getDeclaredField(signatureFieldName);
            signatureField.setAccessible(true);
            String signature = (String) signatureField.get(obj);

            if (!cacuSignature.equals(signature)) {
                return false;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.warn("给定的签名字段不存在，签名校验失败");
            return false;
        }

        return true;
    }

    public String getSignature(Object obj) {
        ArrayList<String> fieldDataList = new ArrayList<>(fieldNameList.size());
        for(String fieldName : fieldNameList) {
            try {
                Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                Object originValue = field.get(obj);

                if (originValue == null) {
                    fieldDataList.add(null);
                }
                else {
                    // 枚举型转化为对应的整数
                    if (field.getType().isEnum()) {
                        originValue = ((Enum)originValue).ordinal();
                    }

                    fieldDataList.add(originValue.toString());
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error("给定的用于生成签名的字段不存在，已跳过该字段");
                continue;
            }
        }

        StringBuilder signature = Common.concatString("-", fieldDataList);
        return encrypt(signature.toString());
    }
}
