package cn.edu.xmu.privilegegateway.privilegeservice.controller;

import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import cn.edu.xmu.privilegegateway.privilegeservice.PrivilegeServiceApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;




@AutoConfigureMockMvc
@Transactional
@SpringBootTest(classes = PrivilegeServiceApplication.class)
public class BaseRoleAndPrivilegeTest {
    private static String token;
    private static JwtHelper jwtHelper = new JwtHelper();

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void init() {
        token = jwtHelper.createToken(29L, "zyu", 0L, 0, 36000);
    }
    /*获取权限所有状态*/
    @Test
    public void testGetAllPrivilegesStates() throws Exception{
        String responseString = this.mvc.perform(get("/privileges/states")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":0,\"data\":[{\"code\":0,\"name\":\"GET\"},{\"code\":1,\"name\":\"POST\"},{\"code\":2,\"name\":\"PUT\"},{\"code\":3,\"name\":\"DELETE\"}],\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*添加，新增功能角色权限*/
    @Test
    //错误did
    public void testAddRolePrivilegesByErrordid() throws Exception
    {
        //非功能角色
        String responseString = this.mvc.perform(post("/departs/1/baseroles/0/privileges/0")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,false);
    }
    /*添加新增功能角色权限*/
    @Test
    /*正确测试*/
    public void testAddRolePrivileges() throws Exception{
        String responseString = this.mvc.perform(post("/departs/0/baseroles/80/privileges/2")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected ="{\"errno\":0,\"data\":{\"name\":\"查看任意用户信息\",\"gmtModified\":null,\"creator\":{\"id\":29,\"name\":\"zyu\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":0},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*添加新增功能角色权限 重复权限*/
    @Test
    public void testAddBaseRolePrivilegesWithExistPriv() throws Exception{
        String responseString = this.mvc.perform(post("/departs/0/baseroles/23/privileges/3")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected = "{\"errno\":756,\"errmsg\":\"重复定义权限\"}";
        JSONAssert.assertEquals(expected, responseString, false);
    }
    /*添加新增功能角色权限 不是功能角色*/
    @Test
    public void testAddBaseRolePrivilegesWithErrorRole() throws Exception{
        String responseString = this.mvc.perform(post("/departs/0/baseroles/13/privileges/3")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expected, responseString, false);
    }
    /*删除功能角色权限,不存在的权限*/
    @Test
    public void testDelPrivsByErrorPrivilege() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/0/baseroles/86/privileges/2")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*删除功能角色权限,不是功能角色*/
    @Test
    public void testDelPrivsByErrorRole() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/0/baseroles/13/privileges/2")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,false);
    }
    /*删除功能角色权限,错误did*/
    @Test
    public void testDelPrivsByErrordid() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/1/baseroles/23/privileges/2")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,false);
    }
    /*删除功能角色权限,成功*/
    @Test
    public void testDelBaseRolePrivs() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/0/baseroles/23/privileges/2")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":0,\"errmsg\":\"成功\"}", responseString,false);
    }
    /*查询功能角色权限*/
    @Test
    public void testSelectBaseRolePrivs() throws  Exception
    {
        String responseString = this.mvc.perform(get("/departs/0/baseroles/23/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .param("page","1")
                .param("pageSize","10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String exstr="{\"errno\":0,\"data\":{\"total\":10,\"pages\":1,\"pageSize\":10,\"page\":1,\"list\":[{\"id\":2,\"name\":\"查看任意用户信息\",\"gmtCreate\":\"2020-11-01T10:11:21.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":3,\"name\":\"修改任意用户信息\",\"gmtCreate\":\"2020-11-01T10:11:53.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":4,\"name\":\"删除用户\",\"gmtCreate\":\"2020-11-01T10:12:15.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":5,\"name\":\"恢复用户\",\"gmtCreate\":\"2020-11-01T10:12:15.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":6,\"name\":\"禁止用户登录\",\"gmtCreate\":\"2020-11-01T10:12:15.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":7,\"name\":\"赋予用户角色\",\"gmtCreate\":\"2020-11-01T10:12:15.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":8,\"name\":\"取消用户角色\",\"gmtCreate\":\"2020-11-01T10:12:15.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":9,\"name\":\"新增角色\",\"gmtCreate\":\"2020-11-01T10:12:15.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":10,\"name\":\"删除角色\",\"gmtCreate\":\"2020-11-01T10:12:15.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1},{\"id\":11,\"name\":\"修改角色信息\",\"gmtCreate\":\"2020-11-01T10:12:15.000\",\"gmtModified\":null,\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(exstr, responseString,false);
    }
    /*查询功能角色权限 错误did*/
    @Test
    public void testSelectPrivsWithErrorDid() throws  Exception
    {
        String responseString = this.mvc.perform(get("/departs/1/baseroles/23/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .param("page","1")
                .param("pageSize","10"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,true);
    }
    /*查询功能角色权限 错误baserole*/
    @Test
    public void testSelectPrivsWithErrorRid() throws  Exception
    {
        String responseString = this.mvc.perform(get("/departs/0/baseroles/0/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .param("page","1")
                .param("pageSize","10"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected=" {\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*查询权限*/
    @Test
    public void getPrivilege() throws  Exception
    {
        String responseString = this.mvc.perform(get("/departs/0/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .param("url","/departs/{id}/adminusers/{id}")
                .param("requestType","0")
                .param("page","1")
                .param("pageSize","10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":0,\"data\":{\"total\":1,\"pages\":1,\"pageSize\":10,\"page\":1,\"list\":[{\"id\":2,\"name\":\"查看任意用户信息\",\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null},\"sign\":1}]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }

    /*查询权限，错误did*/
    @Test
    public void getPrivilegeWithErrordid() throws  Exception
    {
        String responseString = this.mvc.perform(get("/departs/1/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .param("url","/departs/{id}/adminusers/{id}")
                .param("requestType","0")
                .param("page","1")
                .param("pageSize","10"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expected, responseString,true);
    }
    /*查询权限，不存在的参数*/
    @Test
    public void getPrivilegeWithErrorParam() throws  Exception
    {
        String responseString = this.mvc.perform(get("/departs/0/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .param("url","/departs/{id}")
                .param("requestType","2")
                .param("page","1")
                .param("pageSize","10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":0,\"data\":{\"total\":0,\"pages\":0,\"pageSize\":10,\"page\":1,\"list\":[]},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*新增权限*/
    @Test
    public void testAddPrivs() throws  Exception
    {
        String content="{\"name\": \"string\",\"url\": \"string\",\"requestType\": 0}";
        String responseString = this.mvc.perform(post("/departs/0/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(content))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":0,\"data\":{\"name\":\"string\",\"url\":\"string\",\"requestType\":0,\"gmtModified\":null,\"creator\":{\"id\":29,\"name\":\"zyu\"},\"modifier\":{\"id\":null,\"name\":null},\"sign\":0},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*新增权限 错误did*/
    @Test
    public void testAddPrivsWithErrorDid() throws  Exception
    {
        String content="{\"name\": \"string\",\"url\": \"string\",\"requestType\": 0}";
        String responseString = this.mvc.perform(post("/departs/1/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(content))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,false);
    }
    /*新增权限 重复name,url,requestype*/
    @Test
    public void testAddPrivsWithErrorContent() throws  Exception
    {
        String content="{\"name\": \"查看任意用户信息\",\"url\": \"/departs/{id}/adminusers/{id}\",\"requestType\": 0}";
        String responseString = this.mvc.perform(post("/departs/0/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(content))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":742,\"errmsg\":\"权限url/RequestType重复\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*删除权限*/
    @Test
    public void testDelPrivs() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/0/privileges/2")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":0,\"errmsg\":\"成功\"}", responseString,false);
    }
    /*删除权限,错误did*/
    @Test
    public void testDelPrivsWithDid() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/1/privileges/2")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,false);
    }
    /*删除权限,错误pid*/
    @Test
    public void testDelPrivsWithErrorPid() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/0/privileges/0")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*禁用权限*/
    @Test
    // todo
    public void testForbidPrivs() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/0/privileges/21/forbid")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*禁用权限，错误did*/
    @Test
    public void testForbidPrivsWithErrorDid() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/1/privileges/21/forbid")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,false);
    }
    /*禁用权限，权限不存在*/
    @Test
    public void testForbidPrivsWithErrorPid() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/0/privileges/0/forbid")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,false);
    }
    /*解禁权限,错误did*/
    @Test
    public void testReleasePrivsWithErrorDid() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/1/privileges/21/release")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,false);
    }
    /*解禁权限,错误did*/
    @Test
    public void testReleasePrivs() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/0/privileges/21/release")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":0,\"errmsg\":\"成功\"}", responseString,false);
    }
    /*解禁权限,权限不存在*/
    @Test
    public void testReleasePrivsWithErrorPid() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/1/privileges/21/release")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,false);
    }
    @Test
    /*修改权限信息，错误pid*/
    public void testModifyPrivWithErrorPid()throws Exception
    {
        String body="{\"name\": \"string\",\"url\": \"string\", \"requestType\": 0}";
        String responseString = this.mvc.perform(put("/departs/0/privileges/23")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(body))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    @Test
    /*修改权限信息，重复url,requesttype*/
    public void testModifyPrivWithErrorContent()throws Exception
    {
        String body="{\"name\": \"查看任意用户信息\",\"url\": \"/departs/{id}/adminusers/{id}\", \"requestType\": 0}";
        String responseString = this.mvc.perform(put("/departs/0/privileges/13")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":742,\"errmsg\":\"权限url与RequestType重复\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    @Test
    /*修改权限信息，错误参数*/
    public void testModifyPrivWithErrorVo()throws Exception
    {
        String body="{\"name\":null,\"url\": \"string\", \"requestType\": 0}";
        String responseString = this.mvc.perform(put("/departs/0/privileges/23")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":503,\"errmsg\":\"name不得为空;\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    @Test
    /*修改权限信息*/
    public void testModifyPriv()throws Exception
    {
        String body="{\"name\": \"string\",\"url\": \"string\", \"requestType\": 0}";
        String responseString = this.mvc.perform(put("/departs/0/privileges/21")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":0,\"errmsg\":\"成功\"}", responseString,false);
    }
    /*修改权限信息，错误did*/
    @Test
    public void testModifyPrivWithErrorDid()throws Exception
    {
        String body="{\"name\": \"string\",\"url\": \"string\", \"requestType\": 0}";
        String responseString = this.mvc.perform(put("/departs/1/privileges/23")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(body))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,false);
    }
    /*新增权限到redis*/
    @Test
    public void testAddPrivilegeToRedis()throws Exception
    {
        String body="{\"url\": \"/departs/{id}/adminusers/{id}\", \"requestType\": 0}";
        String responseString = this.mvc.perform(put("/internal/privileges/load")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*新增权限到redis，错误的权限信息*/
    @Test
    public void testAddPrivilegeToRedisWithErrorContent()throws Exception
    {
        String body="{\"url\": \"sddddddd\", \"requestType\": 0}";
        String responseString = this.mvc.perform(put("/internal/privileges/load")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }

}

