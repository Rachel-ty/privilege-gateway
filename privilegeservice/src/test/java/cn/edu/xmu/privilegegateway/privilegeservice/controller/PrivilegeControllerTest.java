package cn.edu.xmu.privilegegateway.privilegeservice.controller;

import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.privilegeservice.PrivilegeServiceApplication;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

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

    @MockBean
    private HttpServletRequest request;
    @Autowired
    private UserDao userDao;
    @Autowired
    private MockMvc mvc;
    @MockBean
    private RedisUtil redisUtil;

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
        String expectString2 = "{\"errno\":0,\"data\":{\"total\":0,\"pages\":0,\"pageSize\":10,\"page\":1,\"list\":[]},\"errmsg\":\"成功\"}";
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

    /**
     * Method: resetPassword(@RequestBody ResetPwdVo vo,BindingResult bindingResult
     * , HttpServletResponse httpServletResponse,HttpServletRequest httpServletRequest)
     */
    @Test
    public void testResetPassword() throws Exception {

        String contentJson1="{\"name\":\"minge@163.com\"}";
        String responseString1 = mvc.perform(put("/self/password/reset")
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, false);

    }
    @Test
    public void testModifyPassword() throws Exception {
        String contentJson1="{\n" +
                "  \"name\": \"minge@163.com\",\n" +
                "  \"captcha\": \"123456\",\n" +
                "  \"newPassword\": \"123456\"\n" +
                "}";
        Mockito.when(redisUtil.get("cp_123456")).thenReturn(1L);
        Mockito.when(redisUtil.hasKey("cp_123456")).thenReturn(true);
        String responseString1 = mvc.perform(put("/self/password")
                        .contentType("application/json;charset=UTF-8").content(contentJson1))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":741,\"errmsg\":\"不能与旧密码相同\"}}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
    }
    @Test
    public void testModifyPassword2() throws Exception {
        String contentJson1="{\n" +
                "  \"name\": \"minge@163.com\",\n" +
                "  \"captcha\": \"123456\",\n" +
                "  \"newPassword\": \"12345678\"\n" +
                "}";
        Mockito.when(redisUtil.get("cp_123456")).thenReturn(1L);
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
        String responseString1 = mvc.perform(put("/departs/0/users/61")
                        .contentType("application/json;charset=UTF-8").content(contentJson1).header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
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
        String responseString1 = mvc.perform(delete("/departs/0/users/61")
                        .contentType("application/json;charset=UTF-8").header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
    }
    @Test
    public void testForbidUser() throws Exception {
        String responseString1 = mvc.perform(put("/departs/0/users/61/forbid")
                        .contentType("application/json;charset=UTF-8").header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, true);
        responseString1 = mvc.perform(put("/departs/0/users/61/release")
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

        String responseString1 = mvc.perform(put("/departs/0/users/1/approve")
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
        String responseString1 = mvc.perform(put("/departs/0/users/1/approve")
                        .contentType("application/json;charset=UTF-8").content(contentJson1).header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString1 = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString1, responseString1, false);
    }

    @Test
    public void testAddToDepart() throws Exception {
        String responseString1 = mvc.perform(put("/internal/users/60/departs/0")
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
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals(expectString1, responseString1, false);
        token = jwtHelper.createToken(6L, "jxy", 0L, 1, 36000);
        responseString1 = mvc.perform(put("/internal/users/1/departs/0")
                        .contentType("application/json;charset=UTF-8").header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals(expectString1, responseString1, false);
    }


    }
