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
@AllArgsConstructor
@NoArgsConstructor
public abstract class Sign {
    protected List<String> fieldNameList;

    public Sign(String... fieldNames) {
        fieldNameList = new ArrayList<>();
        fieldNameList.addAll(Arrays.asList(fieldNames));
    }

    public abstract String encrypt(String content);

    public boolean check(Object obj) {
        String cacuSignature = getSignature(obj);
        try {
            Field signatureField = obj.getClass().getDeclaredField("signature");
            signatureField.setAccessible(true);
            String signature = (String) signatureField.get(obj);

            if (!cacuSignature.equals(signature)) {
                return false;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
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
                String originValue = field.get(obj).toString();
                fieldDataList.add(originValue);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                continue;
            }
        }

        StringBuilder signature = Common.concatString("-", fieldDataList);
        return encrypt(signature.toString());
    }
}
