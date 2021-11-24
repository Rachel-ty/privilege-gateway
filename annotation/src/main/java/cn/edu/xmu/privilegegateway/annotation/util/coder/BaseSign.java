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

package cn.edu.xmu.privilegegateway.annotation.util.coder;

import cn.edu.xmu.privilegegateway.annotation.util.Common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wang zhongyu
 * @date 2021-11-22
 * modifiedBy Ming Qiu 2021-11-24 19:04
 */
public abstract class BaseSign {
    private Logger logger = LoggerFactory.getLogger(BaseSign.class);

    /**
     * 具体的加密算法
     * @param content
     * @return
     */
    protected abstract String encrypt(String content);

    public boolean check(Object obj,  List<String> signField, String signTarget) {
        String cacuSignature = getSignature(obj, signField);

        try {
            Field signatureField = obj.getClass().getDeclaredField(signTarget);
            signatureField.setAccessible(true);
            String signature = (String) signatureField.get(obj);

            if (!cacuSignature.equals(signature)) {
                logger.error(String.format("decode_check: 用户签名校验失败，原始签名:%s, 计算签名:%s", signature, cacuSignature));
                return false;
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            logger.error("check: 给定的签名字段不存在，签名校验失败");
            return false;
        }

        return true;
    }

    /**
     * 生成签名
     * @param obj 对象
     * @param signField 签名的构成字段（按顺序）
     * @return
     */
    public String getSignature(Object obj, List<String> signField) {
        ArrayList<String> fieldDataList = new ArrayList<>(signField.size());
        for(String fieldName : signField) {
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
                logger.error("getSignature: 给定的用于生成签名的字段不存在，已跳过该字段");
                return null;
            }
        }

        StringBuilder signature = Common.concatString("-", fieldDataList);
        logger.info(String.format("getSignature: signature = %s", signature));
        return encrypt(signature.toString());
    }
}
