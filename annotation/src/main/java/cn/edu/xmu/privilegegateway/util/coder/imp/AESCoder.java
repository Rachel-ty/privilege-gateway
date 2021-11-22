package cn.edu.xmu.privilegegateway.util.coder.imp;

import cn.edu.xmu.privilegegateway.util.coder.Coder;
import cn.edu.xmu.privilegegateway.util.coder.Sign;
import cn.edu.xmu.privilegegateway.util.encript.AES;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class AESCoder extends Coder {
    public static String AESPASS = "OOAD2020-11-01";

    public AESCoder(Object originObj, Sign sign, String... fieldNames) {
        super(originObj, sign, fieldNames);
    }

    @Override
    public String encrypt(String content) {
        return AES.encrypt(content, AESPASS);
    }

    @Override
    public String decrypt(String content) {
        return AES.decrypt(content, AESPASS);
    }
}
