package cn.edu.xmu.privilegegateway.util.coder;

import cn.edu.xmu.privilegegateway.util.Common;
import cn.edu.xmu.privilegegateway.util.coder.imp.AESCoder;
import cn.edu.xmu.privilegegateway.util.coder.imp.SHA256Sign;
import cn.edu.xmu.privilegegateway.util.encript.AES;
import cn.edu.xmu.privilegegateway.util.encript.SHA256;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author wang zhongyu
 * @date 2021-11-22
 */
public class CoderTest {
    @Test
    public void codeTest() {
        String AESPASS = "OOAD2020-11-01";

        User user = new User();
        user.setName("张三");
        user.setUserName("zhangsan");
        user.setMobile("12345678901");
        user.setEmail("12345678901@163.com");
        user.setPassword("aADASDSADX221");
        user.setOpenId("12345");
        user.setState(User.State.NORM);
        user.setDepartId(123L);
        user.setCreatorId(1L);
        BaseSign sha256Sign = new SHA256Sign("signature","userName", "password", "mobile", "email", "openId", "state", "departId", "creatorId", "test");
        BaseCoder aesCoder = new AESCoder(sha256Sign, "name", "mobile", "email", "test");

        StringBuilder signatureBuilder = Common.concatString("-",
                user.getUserName(),
                user.getPassword(),
                user.getMobile(),
                user.getEmail(),
                user.getOpenId(),
                user.getState().getCode().toString(),
                user.getDepartId().toString(),
                user.getCreatorId().toString());
        String signature = SHA256.getSHA256(signatureBuilder.toString());

        UserPo userPo = (UserPo) aesCoder.code(user, UserPo.class);
        String encryptName = AES.encrypt(user.getName(), AESPASS);
        String encryptMobile = AES.encrypt(user.getMobile(), AESPASS);
        String encryptEmail = AES.encrypt(user.getEmail(), AESPASS);

        Assertions.assertTrue(userPo.getName().equals(encryptName));
        Assertions.assertTrue(userPo.getMobile().equals(encryptMobile));
        Assertions.assertTrue(userPo.getEmail().equals(encryptEmail));
        Assertions.assertTrue(userPo.getSignature().equals(signature));

        // 验证签名生成失败时，返回null
        BaseSign sha256Sign1 = new SHA256Sign("signatureA","userName", "password", "mobile", "email", "openId", "state", "departId", "creatorId");
        BaseCoder aesCoder1 = new AESCoder(sha256Sign1, "name", "mobile", "email");
        UserPo userPo1 = (UserPo) aesCoder1.code(user, UserPo.class);

        Assertions.assertNull(userPo1);
    }

    @Test
    public void decodeTest() {
        String AESPASS = "OOAD2020-11-01";


        User user = new User();
        user.setName("张三");
        user.setUserName("zhangsan");
        user.setMobile("12345678901");
        user.setEmail("12345678901@163.com");
        user.setPassword("aADASDSADX221");
        user.setOpenId("12345");
        user.setDepartId(123L);
        user.setCreatorId(1L);
        BaseSign sha256Sign = new SHA256Sign("signature", "userName", "password", "mobile", "email", "openId", "level", "departId", "creatorId");
        BaseCoder aesCoder = new AESCoder(sha256Sign, "name", "mobile", "email", "test");

        UserPo userPo = (UserPo) aesCoder.code(user, UserPo.class);

        User decodeUser = (User) aesCoder.decode(userPo, User.class);

        String decryptName = AES.decrypt(userPo.getName(), AESPASS);
        String decryptMobile = AES.decrypt(userPo.getMobile(), AESPASS);
        String decryptEmail = AES.decrypt(userPo.getEmail(), AESPASS);

        Assertions.assertTrue(decodeUser.getName().equals(decryptName));
        Assertions.assertTrue(decodeUser.getMobile().equals(decryptMobile));
        Assertions.assertTrue(decodeUser.getEmail().equals(decryptEmail));

        // 测试给定错误的签名字段，解密返回null
        BaseSign sha256Sign1 = new SHA256Sign("signatureA", "userName", "password", "mobile", "email", "openId", "level", "departId", "creatorId");
        BaseCoder aesCoder1 = new AESCoder(sha256Sign1, "name", "mobile", "email");
        User decodeUser1 = (User) aesCoder1.decode(userPo, User.class);
        Assertions.assertNull(decodeUser1);

        // 测试Po对象被篡改后的结果
        userPo.setMobile("03A31E01B015CA97F0F7147CC5A01AB4");
        User decodeUser2 = (User) aesCoder.decode(userPo, User.class);
        Assertions.assertNull(decodeUser2);
    }
}
