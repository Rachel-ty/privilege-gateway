package cn.edu.xmu.privilegegateway.privilegeservice.controller;

import cn.edu.xmu.privilegegateway.annotation.util.JacksonUtil;
import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.privilegeservice.PrivilegeServiceApplication;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.LoginVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.Serializable;
import java.lang.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * @author xiuchen lang 22920192204222
 * @date 2021/11/25 14:07
 * modifier huijing zhang
 * modifier wenkai wang
 */
@AutoConfigureMockMvc
@Transactional
@SpringBootTest(classes = PrivilegeServiceApplication.class)
public class PrivilegeControllerTest {
    private static String token;
    private static String pToken;
    private static String adminToken;
    private static String testToken1;
    private static String token17334;
    private static String token17333;

    private static JwtHelper jwtHelper = new JwtHelper();
    @MockBean
    private RedisUtil redisUtil;
    @Autowired
    private UserDao userDao;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void init() {
        token = jwtHelper.createToken(46L, "个", 0L, 1, 36000);
        token17334 = jwtHelper.createToken(17334L, "个", 0L, 1, 36000);
        token17333 = jwtHelper.createToken(17333L, "个", 0L, 1, 36000);
        pToken = jwtHelper.createToken(60L, "pikaas", 0L, 1, 36000);
        adminToken=jwtHelper.createToken(1L, "13088admin", 0L, 1, 36000);
        testToken1 = jwtHelper.createToken(47L, "user", 2L, 1, 36000);
    }

    /**
     * Method: setUsersProxy(@LoginUser Long proxyUserId, @LoginName String creatorName, @Depart Long departId, @PathVariable("id") Long userId, @Validated @RequestBody UserProxyVo vo, BindingResult bindingresult)
     */
    @Test
    public void testSetUsersProxy() throws Exception {
        //和已有代里时间冲突
        String contentJson = "{\"beginDate\": \"2010-09-07T18:51:42.000\",\"endDate\": \"2030-09-07T18:55:42.000\"}";
        String responseString = mvc.perform(post("/users/17334/proxies").header("authorization", token17333)
                        .contentType("application/json;charset=UTF-8").content(contentJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":747,\"errmsg\":\"同一时间段有冲突的代理关系\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
        //正常插入
        String contentJson1 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString1 = mvc.perform(post("/users/17333/proxies").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"data\":{\"user\":{\"id\":17333,\"name\":null},\"proxyUser\":{\"id\":17334,\"name\":null},\"beginDate\":\"2021-05-03T18:54:29.000\",\"endDate\":\"2021-05-04T18:54:29.000\",\"valid\":0,\"creator\":{\"id\":17334,\"name\":\"个\"},\"modifier\":{\"id\":17334,\"name\":\"个\"},\"sign\":0},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, false);
        //开始时间早于结束时间
        String contentJson2 = "{\"beginDate\": \"2020-05-03T18:54:29.000\",\"endDate\": \"2020-05-02T18:54:29.000\"}";
        String responseString2 = mvc.perform(post("/users/17333/proxies").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8").content(contentJson2))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString2 = "{\"errno\":750,\"errmsg\":\"开始时间要小于失效时间\"}";
        JSONAssert.assertEquals(expectString2, responseString2, true);
        //代理被代理为同一个人
        String contentJson3 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString3 = mvc.perform(post("/users/17334/proxies").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8").content(contentJson3))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString3 = "{\"errno\":751,\"errmsg\":\"自己不可以代理自己\"}";
        JSONAssert.assertEquals(expectString3, responseString3, true);
        //不存在用户
        String contentJson4 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString4 = mvc.perform(post("/users/173330/proxies").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8").content(contentJson4))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString4 = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString4, responseString4, true);
        //不是同一个部门
        String contentJson5 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString5 = mvc.perform(post("/users/57/proxies").header("authorization", token17334)
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
        String responseString1 = mvc.perform(post("/departs/1/users/17333/proxyusers/17334").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":705,\"errmsg\":\"无权限\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
        //开始时间早于结束时间
        String contentJson2 = "{\"beginDate\": \"2020-05-03T18:54:29.000\",\"endDate\": \"2020-05-02T18:54:29.000\"}";
        String responseString2 = mvc.perform(post("/departs/0/users/17333/proxyusers/17334").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8").content(contentJson2))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString2 = "{\"errno\":750,\"errmsg\":\"开始时间要小于失效时间\"}";
        JSONAssert.assertEquals(expectString2, responseString2, true);
        //正常插入
        String contentJson3 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString3 = mvc.perform(post("/departs/0/users/17333/proxyusers/17334").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8").content(contentJson3))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString3 = "{\"errno\":0,\"data\":{\"user\":{\"id\":17333,\"name\":null},\"proxyUser\":{\"id\":17334,\"name\":null},\"beginDate\":\"2021-05-03T18:54:29.000\",\"endDate\":\"2021-05-04T18:54:29.000\",\"valid\":0,\"creator\":{\"id\":17334,\"name\":\"个\"},\"modifier\":{\"id\":17334,\"name\":\"个\"},\"sign\":0},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString3, responseString3, false);
        //代理被代理为同一个人
        String contentJson4 = "{\"beginDate\": \"2021-05-03T18:54:29.000\",\"endDate\": \"2021-05-04T18:54:29.000\"}";
        String responseString4 = mvc.perform(post("/departs/0/users/17334/proxyusers/17334").header("authorization", token17334)
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
        String responseString = mvc.perform(delete("/self/proxies/11").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString, responseString, true);

        //正常删除
        String responseString2 = mvc.perform(delete("/self/proxies/7").header("authorization", token17333)
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
        String responseString1 = mvc.perform(get("/departs/2/proxies?beginTime=1900-11-11T10:10:10.000&endTime=2050-11-11T16:10:10.000").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"data\":{\"total\":1,\"pages\":1,\"pageSize\":10,\"page\":1,\"list\":[{\"id\":7,\"user\":{\"id\":17334,\"name\":null},\"proxyUser\":{\"id\":17333,\"name\":null},\"beginDate\":\"2017-12-05T08:57:32.000\",\"endDate\":\"2019-12-05T08:57:36.000\",\"valid\":1,\"gmtCreate\":\"2021-12-05T00:57:56.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":\"admin\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":0}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);

        String responseString2 = mvc.perform(get("/departs/1/proxies?aid=17334&bid=17333").header("authorization", token17334)
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
        String responseString1 = mvc.perform(delete("/departs/2/proxies/7").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);

        //查不到
        String responseString2 = mvc.perform(delete("/departs/1/proxies/2").header("authorization", token17334)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString2 = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString2, responseString2, true);
    }

    /**
     * @author BingShuai Liu 22920192204245
     * @throws Exception
     */
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

    /**
     * @author BingShuai Liu 22920192204245
     * @throws Exception
     */
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
        String expectString2 = "{\"errno\":0,\"data\":{\"name\":\"刘冰帅\"},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString2, responseString2, false);
    }

    /**
     * @author BingShuai Liu 22920192204245
     * @throws Exception
     */
    @Test
    public void checkSelfInformation() throws Exception {
        //成功
        String responseString = mvc.perform(get("/self/users").header("authorization", pToken)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"id\":60,\"userName\":\"pikaas\",\"mobile\":\"123456789\",\"email\":\"123456789@qq.com\",\"avatar\":null,\"lastLoginTime\":null,\"lastLoginIp\":null,\"state\":1,\"departId\":0,\"idNumber\":null,\"passportNumber\":null,\"creator\":null,\"gmtCreate\":\"2021-11-29T09:09:09\",\"gmtModified\":null,\"modifier\":null,\"sign\":null},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * @author BingShuai Liu 22920192204245
     * @throws Exception
     */
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

    /**
     * @author BingShuai Liu 22920192204245
     * @throws Exception
     */
  /*  @Test
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

    }*/

    /**
     * @author BingShuai Liu 22920192204245
     * @throws Exception
     */
    @Test
    public void getUserInformation() throws Exception {
        //成功
        String responseString = mvc.perform(get("/departs/0/users").header("authorization", pToken)
                        .param("userName","pikaas")
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"code\":\"OK\",\"errmsg\":\"成功\",\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":60,\"userName\":\"pikaas\",\"mobile\":\"123456789\",\"email\":\"123456789@qq.com\",\"avatar\":null,\"lastLoginTime\":null,\"lastLoginIp\":null,\"state\":1,\"departId\":0,\"idNumber\":\"123456789123\",\"passportNumber\":\"123456\",\"creator\":{\"id\":1,\"name\":\"admin\"},\"gmtCreate\":\"2021-11-29T09:09:09\",\"gmtModified\":null,\"modifier\":{\"id\":null,\"name\":null},\"sign\":1}]}}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * @author BingShuai Liu 22920192204245
     * @throws Exception
     */
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
        String expectString = "{\"code\":\"OK\",\"errmsg\":\"成功\",\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":4,\"name\":\"刘冰帅\"}]}}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * @author BingShuai Liu 22920192204245
     * @throws Exception
     */
    @Test
    public void getAnyUserInformation() throws Exception {
        //成功
        String responseString = mvc.perform(get("/departs/0/users/60").header("authorization", pToken)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"id\":60,\"userName\":\"pikaas\",\"mobile\":\"123456789\",\"email\":\"123456789@qq.com\",\"avatar\":null,\"lastLoginTime\":null,\"lastLoginIp\":null,\"state\":1,\"departId\":0,\"idNumber\":null,\"passportNumber\":null,\"creator\":null,\"gmtCreate\":\"2021-11-29T09:09:09\",\"gmtModified\":null,\"modifier\":null,\"sign\":null},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * @author BingShuai Liu 22920192204245
     * @throws Exception
     */
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

    /**
     * @author BingShuai Liu 22920192204245
     * @throws Exception
     */
    @Test
    public void getNewUser() throws Exception {
        //成功
        String responseString = mvc.perform(get("/departs/1/newusers/1").header("authorization", adminToken)
                        .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"id\":1,\"userName\":\"新用户1\",\"mobile\":\"19E59DE959DE472ABECEC38A0219689A\",\"email\":\"123123@qq.com\",\"name\":\"1\",\"avatar\":null,\"openId\":\"123123\",\"departId\":1,\"password\":\"BCB71451C344BFB09FC0403699098E9E\",\"idNumber\":null,\"passportNumber\":null,\"creator\":{\"id\":1,\"name\":\"admin\"},\"gmtCreate\":\"2020-11-18T22:48:24\",\"gmtModified\":null,\"modifier\":{\"id\":null,\"name\":null},\"level\":null},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /* -------------------------------------------------------------------------------------------------------------- */

    //获得角色的所有状态
    @Test
    @Transactional
    void getRoleAllStates() throws Exception {
        String responseString = this.mvc.perform(get("/roles/states")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", testToken1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\": 0,\"data\":[{\"name\":\"正常\",\"code\":0},{\"name\":\"禁用\",\"code\":1}],\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //获得用户的功能角色
    @Test
    @Transactional
    void getBaserolesByUserId() throws Exception {
        Mockito.when(redisUtil.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(redisUtil.set(Mockito.anyString(), Mockito.any(), Mockito.anyLong())).thenReturn(true);

        String responseString = this.mvc.perform(get("/departs/0/users/1/baseroles?pageSize=1")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "";
        expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    //在没有权限的情况下获得用户的功能角色
    @Test
    @Transactional
    void getBaserolesByUserIdWithDifDepartId() throws Exception{
        String responseString = this.mvc.perform(get("/departs/1/users/1/baseroles")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", testToken1))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":503,\"errmsg\":\"departId不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //获得用户的功能角色但传入的部门id和用户id不匹配
    @Test
    @Transactional
    void getBaserolesByUserIdWithNotMatchedDepartId() throws Exception{
        String responseString = this.mvc.perform(get("/departs/1/users/1/baseroles")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"部门id不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //赋予用户角色
    @Test
    @Transactional
    void assignRole() throws Exception {
        String responseString = this.mvc.perform(post("/departs/0/users/1/roles/88")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"id\":88,\"name\":\"预售管理\",\"creator\":{\"id\":1,\"name\":\"13088admin\"},\"modifier\":{\"id\":1,\"name\":\"13088admin\"},\"sign\":0},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    //赋予用户角色，传入的部门号不匹配
    @Test
    @Transactional
    void assignRoleWithDepartNotMatched() throws Exception {
        String responseString = this.mvc.perform(post("/departs/1/users/55/roles/11")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //赋予用户重复的角色
    @Test
    @Transactional
    void assignRoleAgain() throws Exception {
        String responseString = this.mvc.perform(post("/departs/0/users/1/roles/1")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":754,\"errmsg\":\"重复赋予角色\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //取消用户角色
    @Test
    @Transactional
    void revokeRole() throws Exception {
        String responseString = this.mvc.perform(delete("/departs/0/users/1/roles/1")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //取消用户角色，传入的部门号不匹配
    @Test
    @Transactional
    void revokeRoleWithDepartNotMatched() throws Exception {
        String responseString = this.mvc.perform(delete("/departs/1/users/1/roles/23")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //取消不存在的角色
    @Test
    @Transactional
    void revokeRoleNotExist() throws Exception {
        String responseString = this.mvc.perform(delete("/departs/1/users/59/roles/999")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //取消用户未拥有的角色
    @Test
    @Transactional
    void revokeRoleAgain() throws Exception {
        String responseString = this.mvc.perform(delete("/departs/1/users/59/roles/87")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //设置角色的继承关系
    @Test
    @Transactional
    void createRoleInherited() throws Exception {
        String responseString = this.mvc.perform(post("/departs/9/roles/1/childroles/10")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"name\":\"店铺9超级管理员\",\"creator\":{\"id\":1,\"name\":\"13088admin\"},\"modifier\":{\"id\":1,\"name\":\"13088admin\"},\"sign\":0},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    //设置角色的继承关系，传入的角色不存在
    @Test
    @Transactional
    void createRoleInheritedWithoutPrivilege() throws Exception {
        String responseString = this.mvc.perform(post("/departs/1/roles/999/childroles/81")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //重复设置角色的继承关系
    @Test
    @Transactional
    void createRoleInheritedAgain() throws Exception {
        String responseString = this.mvc.perform(post("/departs/0/roles/88/childroles/1")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":754,\"errmsg\":\"重复继承角色\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //要设置的父角色和子角色的部门号不匹配且父角色不是功能角色
    @Test
    @Transactional
    void createRoleInheritedDidNotMatched() throws Exception {
        String responseString = this.mvc.perform(post("/departs/1/roles/2/childroles/1")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    //查询角色的功能角色
    @Test
    @Transactional
    void getBaserolesByRoleId() throws Exception {
        Set set=new HashSet<String>();
        set.add("br_88");
        //为方便测试只添加一个，不然返回结果会一直变化
        Mockito.when(redisUtil.getSet(Mockito.anyString())).thenReturn(set);
        Mockito.when(redisUtil.set(Mockito.anyString(), Mockito.any(), Mockito.anyLong())).thenReturn(true);
        String responseString = this.mvc.perform(get("/departs/0/roles/1/baseroles?pageSize=1")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":88,\"name\":\"预售管理\",\"descr\":\"预售活动的管理\",\"departId\":0,\"creator\":{\"id\":1,\"name\":\"admin\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":null}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    //查询角色的功能角色，但url中的did和角色部门不匹配
    @Test
    @Transactional
    void getBaserolesByRoleIdDidNotMatched() throws Exception {
        String responseString = this.mvc.perform(get("/departs/2/roles/1/baseroles")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"部门id不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    // 分页查询所有角色

    @Test
    @Transactional
    public void selectAllRoles() throws Exception {
        String responseString = this.mvc.perform(get("/departs/1/roles?pageSize=1")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString="{\"errno\":0,\"data\":{\"total\":4,\"pages\":4,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":2,\"name\":\"店铺1超级管理员\",\"descr\":null,\"departId\":1,\"creator\":{\"id\":1,\"name\":\"admin\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":null}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    @Transactional
    public void selectAllRoles_unmatchedDid() throws Exception {
        String responseString = this.mvc.perform(get("/departs/999/roles")
                .header("authorization", testToken1)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":503,\"errmsg\":\"departId不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    // 新增一个角色

    @Test
    @Transactional
    public void insertRole() throws Exception {
        String json = "{\"name\":\"新角色\",\"descr\":\"新角色\"}";

        String responseString = this.mvc.perform(post("/departs/1/roles")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8")
                .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"name\":\"新角色\",\"descr\":\"新角色\",\"departId\":1,\"creator\":{\"id\":1,\"name\":\"13088admin\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":null},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    @Transactional
    public void insertRole_unmatchedDid() throws Exception {
        String json = "{\"name\":\"新角色\",\"descr\":\"新角色\"}";

        String responseString = this.mvc.perform(post("/departs/999/roles")
                .header("authorization", testToken1)
                .contentType("application/json;charset=UTF-8")
                .content(json))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":503,\"errmsg\":\"departId不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }


    // 删除角色

    @Test
    @Transactional
    public void deleteRole() throws Exception {
        String responseString = this.mvc.perform(delete("/departs/0/roles/1")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\": 0,\"errmsg\": \"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    @Test
    @Transactional
    public void deleteRole_unmatchedDid() throws Exception {
        String responseString = this.mvc.perform(delete("/departs/999/roles/87")
                .header("authorization", testToken1)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":503,\"errmsg\":\"departId不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    @Test
    @Transactional
    public void deleteRole_roleNotBelongToDepart() throws Exception {
        String responseString = this.mvc.perform(delete("/departs/2/roles/87")
                .header("authorization", testToken1)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    @Test
    @Transactional
    public void deleteRole_IdNotExist() throws Exception {
        String responseString = this.mvc.perform(delete("/departs/1/roles/999")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    // 修改角色信息

    @Test
    @Transactional
    public void updateRole() throws Exception {
        String json = "{\"name\":\"辅助管理员辅助管理员\",\"desc\":\"一般的管理员一般的管理员\"}";

        String responseString = this.mvc.perform(put("/departs/0/roles/1")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8")
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\": 0,\"errmsg\": \"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    @Test
    @Transactional
    public void updateRole_unmatchedDid() throws Exception {
        String json = "{\"name\":\"辅助管理员辅助管理员\",\"desc\":\"一般的管理员一般的管理员\"}";

        String responseString = this.mvc.perform(put("/departs/999/roles/87")
                .header("authorization", testToken1)
                .contentType("application/json;charset=UTF-8")
                .content(json))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":503,\"errmsg\":\"departId不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    @Test
    @Transactional
    public void updateRole_roleNotBelongToDepart() throws Exception {
        String json = "{\"name\":\"管理员\",\"desc\":\"管理员\"}";

        String responseString = this.mvc.perform(put("/departs/2/roles/87")
                .header("authorization", testToken1)
                .contentType("application/json;charset=UTF-8")
                .content(json))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":505,\"errmsg\":\"部门id不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }



    @Test
    @Transactional
    public void updateRole_idNotExit() throws Exception {
        String json = "{\"name\":\"998877\",\"desc\":\"不存在\"}";

        String responseString = this.mvc.perform(put("/departs/1/roles/999")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8")
                .content(json))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":505,\"errmsg\":\"部门id不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    // 查看任意用户的角色

    @Test
    @Transactional
    public void selectRoles() throws Exception {
        String responseString = this.mvc.perform(get("/departs/0/users/1/roles")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":0,\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":1,\"name\":\"平台超级管理员\",\"descr\":null,\"departId\":0,\"creator\":{\"id\":1,\"name\":\"admin\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":0}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    @Transactional
    public void selectRoles_unmatchedDid() throws Exception {
        String responseString = this.mvc.perform(get("/departs/999/users/1/roles")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", testToken1))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":503,\"errmsg\":\"departId不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    @Test
    @Transactional
    public void selectRoles_userNotBelongToDepart() throws Exception {
        String responseString = this.mvc.perform(get("/departs/2/users/1/roles")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", testToken1))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":505,\"errmsg\":\"部门id不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    // 查看自己的角色

    @Test
    @Transactional
    public void selectSelfRoles() throws Exception {
        String responseString = this.mvc.perform(get("/self/roles?pageSize=1")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":0,\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":1,\"name\":\"平台超级管理员\",\"descr\":null,\"departId\":0,\"creator\":{\"id\":1,\"name\":\"admin\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":0}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    // 查看自己的功能角色

    @Test
    @Transactional
    public void selectSelfBaseRoles() throws Exception {
        Mockito.when(redisUtil.get(Mockito.anyString())).thenReturn(null);
        Mockito.when(redisUtil.set(Mockito.anyString(), Mockito.any(), Mockito.anyLong())).thenReturn(true);

        String responseString = this.mvc.perform(get("/self/baseroles?pageSize=1")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    //查询父角色
    @Test
    @Transactional
    public void selectParentRoles() throws Exception {
        String responseString = this.mvc.perform(get("/departs/0/roles/1/parents?pageSize=1")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":0,\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":88,\"name\":\"预售管理\",\"descr\":\"预售活动的管理\",\"departId\":0,\"creator\":{\"id\":1,\"name\":\"admin\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":0}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    @Transactional
    public void selectParentRoles_unmatchedDid() throws Exception {
        String responseString = this.mvc.perform(get("/departs/999/roles/80/parents")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":505,\"errmsg\":\"操作的角色id不在该部门\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    // 禁用角色

    @Test
    @Transactional
    public void forbidRoleTest() throws Exception {
        String responseString = this.mvc.perform(put("/departs/0/roles/1/forbid")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\": 0,\"errmsg\": \"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    @Transactional
    public void forbidRoleTest_unmatchedDid() throws Exception {
        String responseString = this.mvc.perform(put("/departs/999/roles/87/forbid")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", testToken1))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":503,\"errmsg\":\"departId不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    // 解禁角色

    @Test
    @Transactional
    public void releaseRoleTest() throws Exception {
        String responseString = this.mvc.perform(put("/departs/0/roles/1/release")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\": 0,\"errmsg\": \"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    @Transactional
    public void releaseRoleTest_unmatchedDid() throws Exception {
        String responseString = this.mvc.perform(put("/departs/999/roles/87/release")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", testToken1))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":503,\"errmsg\":\"departId不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 测试
     * Created by 22920192204219 蒋欣雨 at 2021/11/29
     */

    @Test
    public void testResetPassword() throws Exception {
        Mockito.when(redisUtil.hasKey(Mockito.anyString())).thenReturn(false);
        Mockito.when(redisUtil.set(Mockito.anyString(), Mockito.any(), Mockito.anyLong())).thenReturn(true);
        String contentJson1 = "{\"name\":\"jxy123\"}";
        String responseString1 = mvc.perform(put("/self/password/reset")
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, false);
        contentJson1 = "{\"name\":\"\"}";
        responseString1 = mvc.perform(put("/self/password/reset")
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        expectString1 = "{\"errno\":503,\"errmsg\":\"不能为空;\"}";
        JSONAssert.assertEquals(expectString1, responseString1, false);
    }
    @Test
    public void testModifyPassword() throws Exception {
        String contentJson1="{\n" +
                "  \"name\": \"minge@163.com\",\n" +
                "  \"captcha\": \"123456\",\n" +
                "  \"newPassword\": \"123456\"\n" +
                "}";
        Mockito.when(redisUtil.get("cp_123456")).thenReturn(62L);
        Mockito.when(redisUtil.hasKey("cp_123456")).thenReturn(true);
        String responseString1 = mvc.perform(put("/self/password")
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":741,\"errmsg\":\"不能与旧密码相同\"}}";
        JSONAssert.assertEquals(expectString1, responseString1, true);

        Mockito.when(redisUtil.hasKey("cp_123456")).thenReturn(false);
        responseString1 = mvc.perform(put("/self/password")
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        expectString1 = "{\"errno\":700,\"errmsg\":\"用户名不存在或者密码错误\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
    }
    @Test
    public void testModifyPassword2() throws Exception {
        String contentJson1="{\n" +
                "  \"name\": \"minge@163.com\",\n" +
                "  \"captcha\": \"123456\",\n" +
                "  \"newPassword\": \"12345678\"\n" +
                "}";
        Mockito.when(redisUtil.get("cp_123456")).thenReturn(62L);
        Mockito.when(redisUtil.hasKey("cp_123456")).thenReturn(true);
        String responseString1 = mvc.perform(put("/self/password")
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
    }

    @Test
    public void testReleaseUser() throws Exception {
        token = jwtHelper.createToken(6L, "jxy", 1L, 1, 36000);
        String responseString1 = mvc.perform(put("/departs/1/users/57/release")
                        .contentType("application/json;charset=UTF-8").header("authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
    }
    @Test
    public void testModifyUserInfo() throws Exception {
        String contentJson1="{\n" +
                "  \"name\": \"jxy\",\n" +
                "  \"avatar\": \"1.jpg\",\n" +
                "  \"idNumber\": \"430124200000000000\",\n" +
                "  \"passportNumber\": \"123456\",\n" +
                "  \"level\": 0\n" +
                "}";
        String responseString1 = mvc.perform(put("/departs/0/users/0")
                        .contentType("application/json;charset=UTF-8").content(contentJson1).header("authorization", token))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = " {\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);

        responseString1 = mvc.perform(put("/departs/0/users/17341")
                        .contentType("application/json;charset=UTF-8").content(contentJson1).header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        expectString1 = "{\"errno\":511,\"errmsg\":\"信息签名不正确\"}";
        JSONAssert.assertEquals(expectString1, responseString1, false);

        responseString1 = mvc.perform(put("/departs/0/users/62")
                        .contentType("application/json;charset=UTF-8").content(contentJson1).header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
    }
    @Test
    public void testModifyUserInfo2() throws Exception {
        token = jwtHelper.createToken(6L, "jxy", 1L, 1, 36000);
        String contentJson1="{\n" +
                "  \"name\": \"jxy\",\n" +
                "  \"avatar\": \"1.jpg\",\n" +
                "  \"idNumber\": \"430124200000000000\",\n" +
                "  \"passportNumber\": \"123456\",\n" +
                "  \"level\": 0\n" +
                "}";

        String responseString1 = mvc.perform(put("/departs/1/users/57")
                        .contentType("application/json;charset=UTF-8").content(contentJson1).header("authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
    }
    @Test
    public void testDeleteUser() throws Exception {
        String responseString1 = mvc.perform(delete("/departs/0/users/62")
                        .contentType("application/json;charset=UTF-8").header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
    }
    @Test
    public void testForbidUser() throws Exception {
        String responseString1 = mvc.perform(put("/departs/0/users/62/forbid")
                        .contentType("application/json;charset=UTF-8").header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
        responseString1 = mvc.perform(put("/departs/0/users/62/release")
                        .contentType("application/json;charset=UTF-8").header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
    }
    @Test
    public void testApproveUser() throws Exception {
        String contentJson1="{\"approve\": \"true\"}";

        String responseString1 = mvc.perform(put("/departs/0/newusers/4/approve")
                        .contentType("application/json;charset=UTF-8").content(contentJson1).header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, false);
    }
    @Test
    public void testApproveUser2() throws Exception {
        String contentJson1="{\"approve\": \"false\"}";
        String responseString1 = mvc.perform(put("/departs/0/newusers/1/approve")
                        .contentType("application/json;charset=UTF-8").content(contentJson1).header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, false);
    }

    @Test
    public void testAddToDepart() throws Exception {
        String responseString1 = mvc.perform(put("/internal/users/62/departs/0")
                        .contentType("application/json;charset=UTF-8").header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, false);

        expectString1 = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        token = jwtHelper.createToken(6L, "jxy", 1L, 1, 36000);
        responseString1 = mvc.perform(put("/internal/users/60/departs/1")
                        .contentType("application/json;charset=UTF-8").header("authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals(expectString1, responseString1, false);
        token = jwtHelper.createToken(6L, "jxy", 0L, 1, 36000);
        responseString1 = mvc.perform(put("/internal/users/1/departs/0")
                        .contentType("application/json;charset=UTF-8").header("authorization", token))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals(expectString1, responseString1, false);
    }


   /**
     * load权限
     * @author RenJieZheng 22920192204334
     * @throws Exception
     */
    @Test
    public void testLoadUserPrivilege() throws Exception {
        Set<Serializable> brKeys = new HashSet<>();
        brKeys.add(0);
        brKeys.add("br_88");
        brKeys.add("br_96");
        Mockito.when(redisUtil.getSet("fu_55")).thenReturn(brKeys);
        JwtHelper jwtHelper = new JwtHelper();
        String adminToken = jwtHelper.createToken(1L, "13088admin", 0L, 2, 3600);
        //以下是正常情况返回的
        String responseString;
        responseString = this.mvc.perform(MockMvcRequestBuilders.put("/internal/users/1/privileges/load")
                .contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString,responseString,false);

        String adminToken1 = jwtHelper.createToken(57L, "comment", 0L, 2, 3600);
        //以下是正常情况返回的
        String responseString1;
        responseString1 = this.mvc.perform(MockMvcRequestBuilders.put("/internal/users/57/privileges/load")
                .contentType("application/json;charset=UTF-8").header("authorization", adminToken1))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString1 = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString1,responseString1,false);

        String adminToken2 = jwtHelper.createToken(17333L, "shop2_auth", 2L, 1, 3600);
        //以下是正常情况返回的
        String responseString2;
        responseString2 = this.mvc.perform(MockMvcRequestBuilders.put("/internal/users/17333/privileges/load")
                .contentType("application/json;charset=UTF-8").header("authorization", adminToken2))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString2 = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString2,responseString2,false);


        String adminToken3 = jwtHelper.createToken(17335L, "shop1_both", 1L, 2, 3600);
        //以下是正常情况返回的
        String responseString3;
        responseString3 = this.mvc.perform(MockMvcRequestBuilders.put("/internal/users/17335/privileges/load")
                .contentType("application/json;charset=UTF-8").header("authorization", adminToken3))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString3 = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString3,responseString3,false);

        String adminToken4 = jwtHelper.createToken(55L, "5961900008", 10L, 2, 3600);
        //以下是正常情况返回的
        String responseString4;
        responseString4 = this.mvc.perform(MockMvcRequestBuilders.put("/internal/users/55/privileges/load")
                .contentType("application/json;charset=UTF-8").header("authorization", adminToken4))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString4 = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString4,responseString4,false);


    }

    /**
     * load权限
     * @author RenJieZheng 22920192204334
     * @throws Exception
     */
    @Test
    public void testLogin() throws Exception {
        Set<Serializable> brKeys = new HashSet<>();
        brKeys.add(0);
        brKeys.add("br_88");
        brKeys.add("br_96");
        Mockito.when(redisUtil.getSet("fu_55")).thenReturn(brKeys);
        LoginVo loginVo = new LoginVo();
        loginVo.setName("13088admin");
        loginVo.setPassword("123456");
        String json = JacksonUtil.toJson(loginVo);
        //以下是正常情况返回的
        String responseString;
        assert json != null;
        responseString = this.mvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType("application/json;charset=UTF-8").content(json))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString,responseString,false);


        LoginVo loginVo2 = new LoginVo();
        loginVo2.setName("shop2_both");
        loginVo2.setPassword("123456");
        String json2 = JacksonUtil.toJson(loginVo2);
        //以下是正常情况返回的
        String responseString2;
        assert json2 != null;
        responseString2 = this.mvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType("application/json;charset=UTF-8").content(json2))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString2 = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString2,responseString2,false);

        LoginVo loginVo3 = new LoginVo();
        loginVo3.setName("comment");
        loginVo3.setPassword("123456");
        String json3 = JacksonUtil.toJson(loginVo3);
        //以下是正常情况返回的
        String responseString3;
        assert json3 != null;
        responseString3 = this.mvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType("application/json;charset=UTF-8").content(json3))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString3 = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString3,responseString3,false);

        LoginVo loginVo4 = new LoginVo();
        loginVo4.setName("shop2_auth");
        loginVo4.setPassword("123456");
        String json4 = JacksonUtil.toJson(loginVo4);
        //以下是正常情况返回的
        String responseString4;
        assert json4 != null;
        responseString4 = this.mvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType("application/json;charset=UTF-8").content(json4))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString4 = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString4,responseString4,false);

        LoginVo loginVo5 = new LoginVo();
        loginVo5.setName("5961900008");
        loginVo5.setPassword("123456");
        String json5 = JacksonUtil.toJson(loginVo5);
        //以下是正常情况返回的
        String responseString5;
        assert json5 != null;
        responseString5 = this.mvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType("application/json;charset=UTF-8").content(json5))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString5 = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString5,responseString5,false);

        LoginVo loginVo1 = new LoginVo();
        loginVo1.setName("13088admin");
        loginVo1.setPassword("1234567");
        String json1 = JacksonUtil.toJson(loginVo1);
        //密码错误
        assert json1 != null;
        responseString = this.mvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType("application/json;charset=UTF-8").content(json1))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        expectedString = "{\n" +
                "\"errno\": 700,\n" +
                "\"errmsg\": \"用户名不存在或者密码错误\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString,responseString,false);

        //手机号登录
        LoginVo loginVo6 = new LoginVo();
        loginVo6.setName("12334555");
        loginVo6.setPassword("123456");
        String json6 = JacksonUtil.toJson(loginVo6);
        //以下是正常情况返回的
        String responseString6;
        assert json6 != null;
        responseString6 = this.mvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType("application/json;charset=UTF-8").content(json6))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString6 = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString6,responseString6,false);

        //邮箱登录
        LoginVo loginVo7 = new LoginVo();
        loginVo7.setName("admin@112");
        loginVo7.setPassword("123456");
        String json7 = JacksonUtil.toJson(loginVo);
        //以下是正常情况返回的
        String responseString7;
        assert json7 != null;
        responseString7 = this.mvc.perform(MockMvcRequestBuilders.post("/login")
                .contentType("application/json;charset=UTF-8").content(json7))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString7 = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString7,responseString7,false);
    }


    /**
     * load权限
     * @author RenJieZheng 22920192204334
     * @throws Exception
     */
    @Test
    public void testLogout() throws Exception{
        JwtHelper jwtHelper = new JwtHelper();
        String adminToken = jwtHelper.createToken(1L, "13088admin", 0L, 1, 3600);

        //以下是正常情况返回的
        String responseString;
        responseString = this.mvc.perform(MockMvcRequestBuilders.get("/logout")
                .contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString = "{\n" +
                "\"errno\": 0,\n" +
                "\"errmsg\": \"成功\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString,responseString,false);
        
        String responseString1 = this.mvc.perform(MockMvcRequestBuilders.get("/logout")
                .contentType("application/json;charset=UTF-8").header("authorization", "this is test"))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String expectedString1 = "{\n" +
                "\"errno\": 501,\n" +
                "\"errmsg\": \"JWT不合法\"\n" +
                "}";
        JSONAssert.assertEquals(expectedString1,responseString1,false);
    }


    /**
     * 新增功能角色
     * @author 张晖婧
     * @throws Exception
     */

    @Test
    @Transactional
    public void insertBaseRole() throws Exception {
        String json = "{\"name\":\"新功能角色\",\"descr\":\"新功能角色\"}";

        String responseString = this.mvc.perform(post("/departs/0/baseroles")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8")
                .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":{\"name\":\"新功能角色\",\"descr\":\"新功能角色\",\"departId\":0,\"creator\":{\"id\":1,\"name\":\"13088admin\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":null},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    @Test
    @Transactional
    public void insertBaseRole_unmatchedDid() throws Exception {
        String json = "{\"name\":\"新功能角色\",\"descr\":\"新功能角色\"}";

        String responseString = this.mvc.perform(post("/departs/999/baseroles")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8")
                .content(json))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    // 查找角色中用户

    @Test
    @Transactional
    public void selectUserByRole() throws Exception {
        String responseString = this.mvc.perform(get("/departs/0/roles/1/users?pageSize=1")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString="{\"errno\":0,\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":1,\"name\":\"admin\",\"descr\":null,\"departId\":0,\"creator\":null,\"modifier\":null,\"sign\":0}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    // 查找角色中用户但部门号不匹配

    @Test
    @Transactional
    public void selectUserByRoleWithDepartNotMatched() throws Exception {
        String responseString = this.mvc.perform(get("/departs/1/roles/1/users?pageSize=1")
                .header("authorization", adminToken)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString;
        expectString="{\"errno\":505,\"errmsg\":\"部门id不匹配\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }
}
