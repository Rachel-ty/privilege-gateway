package cn.edu.xmu.privilegegateway.annotation.util.coder;

import cn.edu.xmu.privilegegateway.annotation.model.User;
import cn.edu.xmu.privilegegateway.annotation.model.UserPo;
import cn.edu.xmu.privilegegateway.annotation.util.coder.imp.AESCoder;
import cn.edu.xmu.privilegegateway.annotation.util.coder.imp.SHA256Sign;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
/**
 * @author wang zhongyu
 * @date 2021-11-22
 * modifiedBy Ming Qiu 2021-11-24 21:05
 */
public class CoderTest {
    BaseCoder coder = new AESCoder(new SHA256Sign(), "OOAD2020-11-01");

    @Test
    public void decodeTest1() {
        UserPo userPo = new UserPo();
        userPo.setName("6E1A67D0B62D40F1145CA7FCA54AD036");
        userPo.setUserName("zhangsan");
        userPo.setMobile("19E59DE959DE472ABECEC38A0219689A");
        userPo.setEmail("167A3FDD80B03DD24B9FBA3B08574263");
        userPo.setPassword("BCB71451C344BFB09FC0403699098E9E");
        userPo.setOpenId("12345");
        userPo.setState((byte) 1);
        userPo.setDepartId(123L);
        userPo.setCreatorId(1L);
        userPo.setSignature("b52d494a0f2e926675816d4d341f1f12944080eddea75ee741d3392726a91d44");

        Collection<String>  codeFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile"));
        List<String> signFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile","state","departId","level"));
        User user1 = (User) coder.decode_check(userPo, User.class , codeFields, signFields, "signature");
        assertNotNull(user1.getSignature());
        assertEquals("随", user1.getName());
        assertEquals("16978955874", user1.getMobile());
        assertEquals("minge@163.com", user1.getEmail());
        assertEquals("123456", user1.getPassword());
    }

    @Test
    public void decodeTest2() {
        UserPo userPo = new UserPo();
        userPo.setName("DBA85F012E0AEB0D544DA57450B89D78");
        userPo.setUserName("zhangsan");
        userPo.setMobile("7FC0128D40DF298E34E77DCB7AAB3B62");
        userPo.setEmail("C1CD9B50A12262FFDB07BBFFB0DC6237");
        userPo.setPassword("BCB71451C344BFB09FC0403699098E9E");
        userPo.setOpenId("12345");
        userPo.setState((byte) 1);
        userPo.setDepartId(123L);
        userPo.setCreatorId(1L);
        userPo.setSignature("68c39a26b45733a555d1178f16d274bf38261447559371cfd82a083b9cc00b34");

        Collection<String>  codeFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile"));
        List<String> signFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile","state","departId","level"));
        User user1 = (User) coder.decode_check(userPo, User.class , codeFields, signFields, "signature");
        assertNotNull(user1.getSignature());
        assertEquals("个", user1.getName());
        assertEquals("15983424556", user1.getMobile());
        assertEquals("ees@123.com", user1.getEmail());
        assertEquals("123456", user1.getPassword());
    }

    @Test
    public void decodeTest3() {
        UserPo userPo = new UserPo();
        userPo.setName("DBA85F012E0AEB0D544DA57450B89D78");
        userPo.setUserName("zhangsan");
        userPo.setMobile("7FC0128D40DF298E34E77DCB7AAB3B62");
        userPo.setEmail("C1CD9B50A12262FFDB07BBFFB0DC6237");
        userPo.setPassword("BCB71451C344BFB09FC0403699098E9E");
        userPo.setOpenId("12345");
        userPo.setState((byte) 1);
        userPo.setDepartId(123L);
        userPo.setCreatorId(1L);
        userPo.setSignature("68c39a26b45733a555d1178f16d274bf38261447559371cfd82a083b9cc00b34");

        Collection<String>  codeFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile"));
        List<String> signFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile","state","departId","level"));
        UserPo userPo1 = (UserPo) coder.decode_check(userPo, null , codeFields, signFields, "signature");
        assertNotNull(userPo.getSignature());
        assertEquals("个", userPo1.getName());
        assertEquals("15983424556", userPo1.getMobile());
        assertEquals("ees@123.com", userPo1.getEmail());
        assertEquals("123456", userPo1.getPassword());

        User user1 = (User) coder.decode_check(userPo, User.class , null, signFields, "signature");
        assertNotNull(user1.getSignature());
        assertEquals("DBA85F012E0AEB0D544DA57450B89D78", user1.getName());
        assertEquals("7FC0128D40DF298E34E77DCB7AAB3B62", user1.getMobile());
        assertEquals("C1CD9B50A12262FFDB07BBFFB0DC6237", user1.getEmail());
        assertEquals("BCB71451C344BFB09FC0403699098E9E", user1.getPassword());
    }

    @Test
    public void encodeTest1(){
        User user = new User();
        user.setName("你好");
        user.setUserName("aaaaa");
        user.setMobile("13112244");
        user.setEmail("mingqiu@xmu.edu.cn");
        user.setPassword("44555");
        user.setOpenId("12345");
        user.setState(User.State.NEW);
        user.setDepartId(123L);
        user.setCreatorId(1L);
        user.setLevel(0);

        Collection<String>  codeFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile"));
        List<String> signFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile","state","departId","level"));

        UserPo userPo = (UserPo) coder.code_sign(user, UserPo.class, codeFields, signFields, "signature");
        assertNotNull(userPo.getSignature());
        assertEquals("AE6FBCE630F0F0DC8DB292E1B8930B88", userPo.getName());
        assertEquals("59E386459AF5D61D4F60CB98E9C563B3", userPo.getMobile());
        assertEquals("D52C16A52D7FAD7AF5CD8131F3A282DD5AF5A7CAF7F164B0C093E08AC27D0F9E", userPo.getEmail());
        assertEquals("945B8CB9CF2B67C8663E6D213A128D37", userPo.getPassword());

        User user1 = (User) coder.decode_check(userPo, User.class , codeFields, signFields, "signature");
        assertEquals("你好", user1.getName());
        assertEquals("13112244", user1.getMobile());
        assertEquals("mingqiu@xmu.edu.cn", user1.getEmail());
        assertEquals("44555", user1.getPassword());

        userPo.setLevel(2);
        User user3 = (User) coder.decode_check(userPo, User.class , codeFields, signFields, "signature");
        assertNull(user3.getSignature());


    }

    @Test
    public void encodeTest2(){
        User user = new User();
        user.setName("你好");
        user.setUserName("aaaaa");
        user.setMobile("13112244");
        user.setEmail("mingqiu@xmu.edu.cn");
        user.setPassword("44555");
        user.setOpenId("12345");
        user.setState(User.State.NEW);
        user.setDepartId(123L);
        user.setCreatorId(1L);
        user.setLevel(0);

        Collection<String>  codeFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile"));

        UserPo userPo = (UserPo) coder.code(user, UserPo.class, codeFields);
        assertNotNull(userPo);
        assertEquals("AE6FBCE630F0F0DC8DB292E1B8930B88", userPo.getName());
        assertEquals("59E386459AF5D61D4F60CB98E9C563B3", userPo.getMobile());
        assertEquals("D52C16A52D7FAD7AF5CD8131F3A282DD5AF5A7CAF7F164B0C093E08AC27D0F9E", userPo.getEmail());
        assertEquals("945B8CB9CF2B67C8663E6D213A128D37", userPo.getPassword());

        User user1 = (User) coder.decode(userPo, User.class , codeFields);
        assertEquals("你好", user1.getName());
        assertEquals("13112244", user1.getMobile());
        assertEquals("mingqiu@xmu.edu.cn", user1.getEmail());
        assertEquals("44555", user1.getPassword());

        userPo.setLevel(2);
        User user4 = (User) coder.decode(userPo, User.class , codeFields);
        assertNotNull(user4);
    }

    @Test
    public void encodeTest3(){
        UserPo userPo = new UserPo();
        userPo.setName("DBA85F012E0AEB0D544DA57450B89D78");
        userPo.setUserName("zhangsan");
        userPo.setMobile("7FC0128D40DF298E34E77DCB7AAB3B62");
        userPo.setEmail("C1CD9B50A12262FFDB07BBFFB0DC6237");
        userPo.setPassword("BCB71451C344BFB09FC0403699098E9E");
        userPo.setOpenId("12345");
        userPo.setState((byte) 1);
        userPo.setDepartId(123L);
        userPo.setCreatorId(1L);
        userPo.setSignature("68c39a26b45733a555d1178f16d274bf38261447559371cfd82a083b9cc00b34");

        Collection<String>  codeFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile"));
        List<String> signFields = new ArrayList<>(Arrays.asList("password", "name", "email", "mobile","state","departId","level"));
        UserPo user1 = (UserPo) coder.decode_check(userPo, null , codeFields, signFields, "signature");
        assertNotNull(user1.getSignature());
        assertEquals("个", user1.getName());
        assertEquals("15983424556", user1.getMobile());
        assertEquals("ees@123.com", user1.getEmail());
        assertEquals("123456", user1.getPassword());
    }
}
