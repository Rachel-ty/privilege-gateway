package cn.edu.xmu.privilegegateway.util.coder.imp;

import cn.edu.xmu.privilegegateway.util.coder.BaseCoder;
import cn.edu.xmu.privilegegateway.util.coder.BaseSign;
import cn.edu.xmu.privilegegateway.util.encript.AES;

/**
 * @author wang zhongyu
 * @date 2021-11-22
 */
public class AESCoder extends BaseCoder {
    public static String AESPASS = "OOAD2020-11-01";

    public AESCoder(BaseSign sign, String... fieldNames) {
        super(sign, fieldNames);
    }

    @Override
    protected String encrypt(String content) {
        return AES.encrypt(content, AESPASS);
    }

    @Override
    protected String decrypt(String content) {
        return AES.decrypt(content, AESPASS);
    }
}
