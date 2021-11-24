package cn.edu.xmu.privilegegateway.util.coder.imp;

import cn.edu.xmu.privilegegateway.util.coder.BaseSign;
import cn.edu.xmu.privilegegateway.util.encript.SHA256;

/**
 * @author wang zhongyu
 * @date 2021-11-22
 */
public class SHA256Sign extends BaseSign {
    public SHA256Sign(String signatureFieldName, String... fieldNames) {
        super(signatureFieldName, fieldNames);
    }

    @Override
    protected String encrypt(String content) {
        return SHA256.getSHA256(content);
    }
}
