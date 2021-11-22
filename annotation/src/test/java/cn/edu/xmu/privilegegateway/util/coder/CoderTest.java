package cn.edu.xmu.privilegegateway.util.coder;

import cn.edu.xmu.privilegegateway.util.coder.imp.AESCoder;
import org.junit.jupiter.api.Test;

public class CoderTest {
    @Test
    public void codeTest() {
        User user = new User();
        user.setMobile("12345678901");
        user.setEmail("12345678901@163.com");
        user.setPassword("ADADAHI2232131293");
        Coder aesCoder = new AESCoder();

    }

    @Test
    public void decodeTest() {

    }
}
