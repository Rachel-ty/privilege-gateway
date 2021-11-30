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
import java.util.Collection;
import java.util.List;

/**
 * @author wang zhongyu
 * @date 2021-11-22
 * modifiedBy Ming Qiu 2021-11-24 19:04
 */
public abstract class BaseCoder {
    private Logger logger = LoggerFactory.getLogger(BaseCoder.class);

    protected BaseSign sign;

    public BaseCoder(){
        super();
    }


    public BaseCoder(BaseSign sign) {
        this.sign = sign;
    }

    /**
     * 具体的加密算法
     * @param content
     * @return
     */
    protected abstract String encrypt(String content);

    /**
     * 具体的解密算法
     * @param content
     * @return 签名生成失败会返回null
     */
    protected abstract String decrypt(String content);

    /**
     * 加密并签名
     * @param originObj 原始对象
     * @param targetClass 目标对象类型，如果null表示不拷贝生成新对象
     * @param codeFields 加密属性 如果null 表示无加密属性
     * @param signFields 签名属性 null代表不检验签名
     * @param signTarget 签名字段 null代表不检验签名
     * @return 加密签名好的目标对象类型
     */
    public Object code_sign(Object originObj, Class targetClass, Collection<String> codeFields, List<String>  signFields, String signTarget) {
        Object target;
        if (targetClass == null) {
            target = originObj;
        }
        else {
            target = Common.cloneVo(originObj, targetClass);
        }

        if (codeFields != null) {
            // 字段加密
            for(String fieldName : codeFields) {
                try {
                    Field field = target.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    String originValue = (String) field.get(target);
                    String encryptValue = encrypt(originValue);
                    field.set(target, encryptValue);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    logger.info("code_sign: 给定的加密字段不存在，已跳过该字段");
                }
            }
        }


        if (signFields != null && signTarget != null) {
            // 生成签名
            String signature = sign.getSignature(originObj, signFields);
            try {
                Field signatureField = target.getClass().getDeclaredField(signTarget);
                signatureField.setAccessible(true);
                signatureField.set(target, signature);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.error("code_sign: 指定的签名字段不存在，存放签名失败:", e.getMessage());
                return null;
            }
        }

        return target;
    }

    /**
     * 加密
     * @param originObj 原始对象
     * @param targetClass   目标对象类型
     * @param codeFields 加密属性
     * @return 加密好的目标对象类型
     */
    public Object code(Object originObj, Class targetClass, Collection<String> codeFields) {
        return code_sign(originObj, targetClass, codeFields, null, null);
    }

    /**
     * 解密并验证签名
     * @param originObj 原始对象
     * @param targetClass   目标对象类型 null表示不拷贝对象
     * @param codeFields 加密属性 null表示无加密属性
     * @param signFields 签名属性 null代表不检验签名
     * @param signTarget 签名字段 null代表不检验签名
     * @return 签名错误也需要返回data对象
     */
    public Object decode_check(Object originObj, Class targetClass, Collection<String> codeFields, List<String>  signFields, String signTarget) {
        Object target;
        if (targetClass == null) {
            target = originObj;
        }
        else {
            target = Common.cloneVo(originObj, targetClass);
        }

        if (codeFields != null) {
            // 字段解密
            for (String fieldName : codeFields) {
                try {
                    Field field = target.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    String originValue = (String) field.get(target);
                    String decryptValue = decrypt(originValue);
                    field.set(target, decryptValue);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    logger.info("decode_check: 给定的解密字段不存在，已跳过该字段");
                }
            }
        }

        logger.info(String.format("decode_check: 解密后的结果 target= %s", target.toString()));

        if (signFields != null && signTarget != null) {
            // 校验签名
            if (!sign.check(target, signFields, signTarget)) {
                return null;
            }
        }

        return target;
    }

    /**
     * 解密
     * @param originObj 原始对象
     * @param targetClass   目标对象类型
     * @param codeFields 加密属性
     * @return 解密后的对象
     */
    public Object decode(Object originObj, Class targetClass, Collection<String> codeFields) {
        return decode_check(originObj, targetClass, codeFields, null, null);
    }
}
