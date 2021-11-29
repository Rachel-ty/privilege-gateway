package cn.edu.xmu.privilegegateway.privilegeservice.controller;

import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import cn.edu.xmu.privilegegateway.privilegeservice.PrivilegeServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.*;

/**
 * @author xiuchen lang 22920192204222
 * @date 2021/11/25 14:07
 */
@AutoConfigureMockMvc
@Transactional
@SpringBootTest(classes = PrivilegeServiceApplication.class)
public class PrivilegeControllerTest {
    private static String token;
    private static JwtHelper jwtHelper = new JwtHelper();

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void init() {
        token = jwtHelper.createToken(46L, "lxc", 0L, 1, 36000);
    }

    /**
     * Method: setUsersProxy(@LoginUser Long proxyUserId, @LoginName String creatorName, @Depart Long departId, @PathVariable("id") Long userId, @Validated @RequestBody UserProxyVo vo, BindingResult bindingresult)
     */
    @Test
    public void testSetUsersProxy() throws Exception {
        //和已有代里时间冲突
        String contentJson = "{\"beginDate\": \"2020-09-07T18:51:42.000\",\"endDate\": \"2020-09-07T18:55:42.000\"}";
        String responseString = mvc.perform(post("/users/49/proxy").header("authorization", token)
                        .contentType("application/json;charset=UTF-8").content(contentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":747,\"errmsg\":\"同一时间段有冲突的代理关系\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
        //正常插入
        String contentJson1 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString1 = mvc.perform(post("/users/49/proxy").header("authorization", token)
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"data\":{\"user\":{\"id\":49,\"name\":\"阿卡前\"},\"proxyUser\":{\"id\":46,\"name\":\"个\"},\"beginDate\":\"2021-05-03T18:54:29.000\",\"endDate\":\"2021-05-04T18:54:29.000\",\"valid\":0,\"creator\":{\"id\":46,\"name\":\"lxc\"},\"modifier\":{\"id\":46,\"name\":\"lxc\"}},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, false);
        //开始时间早于结束时间
        String contentJson2 = "{\"beginDate\": \"2020-05-03T18:54:29.000\",\"endDate\": \"2020-05-02T18:54:29.000\"}";
        String responseString2 = mvc.perform(post("/users/49/proxy").header("authorization", token)
                        .contentType("application/json;charset=UTF-8").content(contentJson2))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString2 = "{\"errno\":750,\"errmsg\":\"开始时间要小于失效时间\"}";
        JSONAssert.assertEquals(expectString2, responseString2, true);
        //代理被代理为同一个人
        String contentJson3 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString3 = mvc.perform(post("/users/46/proxy").header("authorization", token)
                        .contentType("application/json;charset=UTF-8").content(contentJson3))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString3 = "{\"errno\":751,\"errmsg\":\"自己不可以代理自己\"}";
        JSONAssert.assertEquals(expectString3, responseString3, true);
        //不存在用户
        String contentJson4 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString4 = mvc.perform(post("/users/490/proxy").header("authorization", token)
                        .contentType("application/json;charset=UTF-8").content(contentJson4))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString4 = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString4, responseString4, true);
        //不是同一个部门
        String contentJson5 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString5 = mvc.perform(post("/users/57/proxy").header("authorization", token)
                        .contentType("application/json;charset=UTF-8").content(contentJson5))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString5 = "{\"errno\":752,\"errmsg\":\"两个代理双方的部门冲突\"}";
        JSONAssert.assertEquals(expectString5, responseString5, true);
    }

    /**
     * Method: setUsersProxyByAdmin(@LoginUser Long creatorId, @LoginName String creatorName, @PathVariable("did") Long departId, @PathVariable("aid") Long userId, @PathVariable("bid") Long proxyUserId, @Validated @RequestBody UserProxyVo vo, BindingResult bindingresult)
     */
    @Test
    public void testSetUsersProxyByAdmin() throws Exception {
        //没权限
        String contentJson1 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString1 = mvc.perform(post("/departs/1/users/49/proxyusers/46").header("authorization", token)
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":705,\"errmsg\":\"无权限\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
        //开始时间早于结束时间
        String contentJson2 = "{\"beginDate\": \"2020-05-03T18:54:29.000\",\"endDate\": \"2020-05-02T18:54:29.000\"}";
        String responseString2 = mvc.perform(post("/departs/0/users/49/proxyusers/46").header("authorization", token)
                        .contentType("application/json;charset=UTF-8").content(contentJson2))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString2 = "{\"errno\":750,\"errmsg\":\"开始时间要小于失效时间\"}";
        JSONAssert.assertEquals(expectString2, responseString2, true);
        //正常插入
        String contentJson3 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString3 = mvc.perform(post("/departs/0/users/49/proxyusers/46").header("authorization", token)
                        .contentType("application/json;charset=UTF-8").content(contentJson3))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString3 = "{\"errno\":0,\"data\":{\"user\":{\"id\":49,\"name\":\"阿卡前\"},\"proxyUser\":{\"id\":46,\"name\":\"个\"},\"beginDate\":\"2021-05-03T18:54:29.000\",\"endDate\":\"2021-05-04T18:54:29.000\",\"valid\":0,\"creator\":{\"id\":46,\"name\":\"lxc\"},\"modifier\":{\"id\":46,\"name\":\"lxc\"}},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString3, responseString3, false);
        //代理被代理为同一个人
        String contentJson4 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString4 = mvc.perform(post("/departs/0/users/46/proxyusers/46").header("authorization", token)
                        .contentType("application/json;charset=UTF-8").content(contentJson4))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString4 = "{\"errno\":751,\"errmsg\":\"自己不可以代理自己\"}";
        JSONAssert.assertEquals(expectString4, responseString4, false);

    }

    /**
     * Method: removeUserProxy(@PathVariable("id") Long id, @LoginUser Long userId)
     */
    @Test
    public void testRemoveUserProxy() throws Exception {
        //操作的资源id不存在
        String responseString = mvc.perform(delete("/proxies/11").header("authorization", token)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString, responseString, true);

        //正常删除
        String responseString2 = mvc.perform(delete("/proxies/2").header("authorization", token)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString2 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString2, responseString2, true);
    }

    /**
     * Method: getProxies(@PathVariable("did") Long departId, @RequestParam(value = "aid", required = false) Long userId, @RequestParam(value = "bid", required = false) Long proxyUserId, @RequestParam(value = "page", required = false) Integer page, @RequestParam(value = "pageSize", required = false) Integer pageSize)
     */
    @Test
    public void testGetProxies() throws Exception {
        String responseString1 = mvc.perform(get("/departs/0/proxies").header("authorization", token)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":511,\"data\":{\"total\":5,\"pages\":1,\"pageSize\":10,\"page\":1,\"list\":[{\"id\":1,\"user\":{\"id\":49,\"name\":\"阿卡前\"},\"proxyUser\":{\"id\":47,\"name\":\"全文\"},\"beginDate\":\"2020-10-03T18:51:42.000\",\"endDate\":\"2021-11-03T18:51:52.000\",\"valid\":1,\"gmtCreate\":\"2020-11-03T18:52:00.000\",\"gmtModified\":null,\"creator\":{\"id\":null,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":2,\"user\":{\"id\":49,\"name\":\"阿卡前\"},\"proxyUser\":{\"id\":46,\"name\":\"个\"},\"beginDate\":\"2020-05-03T18:52:25.000\",\"endDate\":\"2020-10-03T18:52:31.000\",\"valid\":1,\"gmtCreate\":\"2020-11-03T18:52:37.000\",\"gmtModified\":null,\"creator\":{\"id\":null,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":3,\"user\":{\"id\":49,\"name\":\"阿卡前\"},\"proxyUser\":{\"id\":48,\"name\":\"斯蒂芬\"},\"beginDate\":\"2021-12-03T18:53:01.000\",\"endDate\":\"2022-11-03T18:53:19.000\",\"valid\":1,\"gmtCreate\":\"2020-11-03T18:53:39.000\",\"gmtModified\":null,\"creator\":{\"id\":null,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":4,\"user\":{\"id\":49,\"name\":\"阿卡前\"},\"proxyUser\":{\"id\":50,\"name\":\"速度\"},\"beginDate\":\"2020-11-01T18:53:59.000\",\"endDate\":\"2020-12-03T18:54:07.000\",\"valid\":0,\"gmtCreate\":\"2020-11-03T18:54:17.000\",\"gmtModified\":null,\"creator\":{\"id\":null,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":5,\"user\":{\"id\":49,\"name\":\"阿卡前\"},\"proxyUser\":{\"id\":51,\"name\":\"撕得粉碎\"},\"beginDate\":\"2020-05-03T18:54:29.000\",\"endDate\":\"2020-07-03T18:54:37.000\",\"valid\":1,\"gmtCreate\":\"2020-11-03T18:54:42.000\",\"gmtModified\":null,\"creator\":{\"id\":null,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1}]},\"errmsg\":\"信息签名不正确\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);

        String responseString2 = mvc.perform(get("/departs/0/proxies?aid=49&bid=56").header("authorization", token)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString2="{\"errno\":0,\"data\":{\"total\":0,\"pages\":0,\"pageSize\":10,\"page\":1,\"list\":[]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString2, responseString2, true);
    }

    /**
     * Method: removeAllProxies(@PathVariable("did") Long departId, @PathVariable("id") Long id)
     */
    @Test
    public void testRemoveAllProxies() throws Exception {
        String responseString1 = mvc.perform(delete("/departs/0/proxies/1").header("authorization", token)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);

        //查不到
        String responseString2 = mvc.perform(delete("/departs/1/proxies/2").header("authorization", token)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString2 = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString2, responseString2, true);
    }

    @Test
    public void getUserStates() throws Exception {
        String responseString = mvc.perform(get("/users/states").header("authorization", pToken)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":[{\"code\":0,\"name\":\"正常\"},{\"code\":1,\"name\":\"禁止访问\"}],\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    public void registerNewUser() throws Exception {
        //邮箱已被注册
        String contentJson = "{\n" +
                "  \"userName\": \"pikaas5\",\n" +
                "  \"password\": \"LLl123456!\",\n" +
                "  \"name\": \"刘冰帅\",\n" +
                "  \"mobile\": \"1234567894\",\n" +
                "  \"email\": \"1234567894@qq.com\",\n" +
                "  \"departId\": -1,\n" +
                "  \"idNumber\": 123456789123,\n" +
                "  \"passportNumber\": 123456\n" +
                "}";
        String responseString = mvc.perform(post("/users").header("authorization", pToken)
                        .contentType("application/json;charset=UTF-8").content(contentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":732,\"errmsg\":\"邮箱已被注册\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
        //成功
        String contentJson2 = "{\n" +
                "  \"userName\": \"测试姓名abb\",\n" +
                "  \"password\": \"LLl123456!\",\n" +
                "  \"name\": \"刘冰帅\",\n" +
                "  \"mobile\": \"79846513244\",\n" +
                "  \"email\": \"1234687971@qq.com\",\n" +
                "  \"departId\": -1,\n" +
                "  \"idNumber\": 22072420010428,\n" +
                "  \"passportNumber\": 132456\n" +
                "}";
        String responseString2 = mvc.perform(post("/users").header("authorization", pToken)
                        .contentType("application/json;charset=UTF-8").content(contentJson2))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString2 = "{\"errno\":0,\"data\":{\"userName\":\"测试姓名abb\",\"sign\":0},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString2, responseString2, false);
    }

    @Test
    public void checkSelfInformation() throws Exception {
        //成功
        String responseString = mvc.perform(get("/self/users").header("authorization", pToken)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"id\":60,\"userName\":\"pikaas\",\"mobile\":\"123456789\",\"email\":\"123456789@qq.com\",\"avatar\":null,\"lastLoginTime\":null,\"lastLoginIp\":null,\"state\":null,\"depart_id\":null,\"idNumber\":\"123456789123\",\"passportNumber\":\"123456\",\"creator\":{\"id\":60,\"userName\":\"pikaas\",\"sign\":0},\"gmtCreate\":null,\"gmtModified\":null,\"modifier\":null,\"sign\":0},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }


    @Test
    public void modifyUserInformation() throws Exception {
        String contentJson = "{\n" +
                    "  \"name\": \"pikaaa\",\n" +
                "  \"avatar\": \"12345644\",\n" +
                "  \"idNumber\": \"220723246\",\n" +
                "  \"passportNumber\": \"89789132132\"\n" +
                "}";
        String responseString = mvc.perform(put("/self/users").header("authorization", pToken)
                        .contentType("application/json;charset=UTF-8").content(contentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    public void uploadImg() throws Exception {
        String responseString0;
        Resource resource0 = new ClassPathResource("test.png");
        File file0 = resource0.getFile();
        InputStream inStream0 = new FileInputStream(file0);
        MockMultipartFile mfile0 = new MockMultipartFile("file", "test.png", ContentType.APPLICATION_OCTET_STREAM.toString(), inStream0);
        responseString0 = mvc.perform(MockMvcRequestBuilders.multipart("/self/users/uploadImg")
                        .file(mfile0)
                        .header("authorization", pToken)
                        .contentType("MediaType.MULTIPART_FORM_DATA_VALUE"))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString0 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectedString0,responseString0,false);

    }

    @Test
    public void getUserInformation() throws Exception {
        //成功
        String responseString = mvc.perform(get("/departs/0/users").header("authorization", pToken)
                        .param("userName","pikaas")
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"code\":\"OK\",\"errmsg\":\"成功\",\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":60,\"userName\":\"pikaas\",\"mobile\":\"123456789\",\"email\":\"123456789@qq.com\",\"avatar\":null,\"lastLoginTime\":null,\"lastLoginIp\":null,\"state\":null,\"depart_id\":null,\"idNumber\":\"123456789123\",\"passportNumber\":\"123456\",\"creator\":{\"id\":60,\"userName\":\"pikaas\",\"sign\":0},\"gmtCreate\":null,\"gmtModified\":null,\"modifier\":null,\"sign\":0}]}}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    public void getNewUserInformation() throws Exception {
        String responseString = mvc.perform(get("/departs/0/users/new").header("authorization", pToken)
                        .param("userName","pikaas3")
                        .param("mobile","1234567892")
                        .param("email","1234567892@qq.com")
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"code\":\"OK\",\"errmsg\":\"成功\",\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":4,\"userName\":\"pikaas3\",\"sign\":0}]}}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    public void judgeNewUser() throws Exception {
        //审核通过,返回用户信息
        String contentJson = "{\n" +
                "  \"conclusion\": true,\n" +
                "  \"level\": 1\n" +
                "}";
        String responseString = mvc.perform(put("/departs/0/users/4/audit").header("authorization", pToken)
                        .contentType("application/json;charset=UTF-8").content(contentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"id\":4,\"userName\":\"pikaas3\",\"mobile\":\"1234567892\",\"email\":\"1234567892@qq.com\",\"name\":\"刘冰帅\",\"avatar\":null,\"openId\":null,\"departId\":0,\"password\":\"LLl123456!\",\"creatorId\":null,\"gmtCreate\":\"2021-11-29T09:07:09\",\"modifierId\":null,\"gmtModified\":null,\"idNumber\":\"123456789123\",\"passportNumber\":\"123456\",\"signature\":\"35047662112df1a91ff02e3b9bad048000e918d485ce8ef8411696e6fa8a1362\",\"creatorName\":null,\"modifierName\":null,\"level\":1},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);

    }

    @Test
    public void getAnyUserInformation() throws Exception {
        //成功
        String responseString = mvc.perform(get("/departs/0/users/60").header("authorization", pToken)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"id\":60,\"userName\":\"pikaas\",\"mobile\":\"123456789\",\"email\":\"123456789@qq.com\",\"avatar\":null,\"lastLoginTime\":null,\"lastLoginIp\":null,\"state\":null,\"depart_id\":null,\"idNumber\":\"123456789123\",\"passportNumber\":\"123456\",\"creator\":{\"id\":60,\"userName\":\"pikaas\",\"sign\":0},\"gmtCreate\":null,\"gmtModified\":null,\"modifier\":null,\"sign\":0},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    public void getUserName() throws Exception {
        //成功
        String responseString = mvc.perform(get("/internal/users/60").header("authorization", adminToken)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":\"pikaas\",\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

}
