package cn.edu.xmu.privilegegateway.privilegeservice.controller;

import cn.edu.xmu.privilegegateway.annotation.util.JacksonUtil;
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
        token = jwtHelper.createToken(29L, "zyu", 0L, 1, 36000);
    }
    /*获取权限所有状态*/
    @Test
    public void testGetAllPrivileges() throws Exception{
        String responseString = this.mvc.perform(get("/privileges/states")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected="{\"errno\":0,\"data\":[{\"code\":0,\"name\":\"GET\"},{\"code\":1,\"name\":\"POST\"},{\"code\":2,\"name\":\"PUT\"},{\"code\":3,\"name\":\"DELETE\"}],\"errmsg\":\"成功\"}";
        assertEquals(expected, responseString);
    }
    /*添加功能角色权限*/
    @Test
    //错误did
    public void testAddRolePrivilegesByErrordid() throws Exception
    {
        //非功能角色
        String responseString = this.mvc.perform(post("/departs/1/roles/0/privileges/0")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString);
    }
    /*添加功能角色权限*/
    @Test
    /*正确测试*/
    public void testAddRolePrivileges() throws Exception{
        String responseString = this.mvc.perform(post("/departs/0/roles/80/privileges/2")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expected ="{\"errno\":0,\"data\":{\"name\":\"查看任意用户信息\",\"gmtCreate\":\"2021-11-30 19:21:30.934\",\"gmtModified\":null,\"creator\":{\"id\":29,\"name\":\"zyu\",\"sign\":0},\"modifier\":{\"id\":null,\"name\":null,\"sign\":0},\"sign\":0},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expected, responseString,false);
    }
    /*删除功能角色权限*/
    @Test
    public void testDelPrivsByErrordid() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/1/roles/23/privileges/2")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString);
    }
    /*删除功能角色权限,成功*/
    @Test
    public void testDelBaseRolePrivs() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/0/roles/23/privileges/2")
                .contentType("application/json;charset=UTF-8")
                .header("authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("{\"errno\":0,\"errmsg\":\"成功\"}", responseString);
    }
    /*查询功能角色权限*/
    @Test
    public void testSelectPrivs() throws  Exception
    {
        String responseString = this.mvc.perform(get("/departs/0/baseroles/23/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .param("page","1")
                .param("pageSize","10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String exstr="{\"errno\":0,\"data\":{\"total\":1,\"pages\":1,\"pageSize\":1,\"page\":1,\"list\":[{\"id\":2,\"name\":\"查看任意用户信息\",\"url\":\"/departs/{id}/adminusers/{id}\",\"requestType\":0,\"gmtCreate\":\"2020-11-01 09:52:20.000\",\"gmtModified\":\"2020-11-02 21:51:45.000\",\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null}}]},\"errmsg\":\"成功\"}";
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
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}", responseString,true);
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
        JSONAssert.assertEquals("{\"errno\":0,\"data\":{\"name\":\"string\",\"url\":\"string\",\"requestType\":0,\"gmtCreate\":null,\"gmtModified\":null,\"creator\":{\"id\":0,\"name\":null},\"modifier\":{\"id\":null,\"name\":null}},\"errmsg\":\"成功\"}", responseString,false);
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
        assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString);
    }
    /*新增权限 重复url*/
    @Test
    public void testAddPrivsWithErrorContent() throws  Exception
    {
        String content="{\"name\": \"string\",\"url\": \"/departs/{id}/roles\",\"requestType\": 1}";
        String responseString = this.mvc.perform(post("/departs/0/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(content))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("{\"errno\":742,\"errmsg\":\"权限url与RequestType重复\"}", responseString);
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
        JSONAssert.assertEquals("{\"errno\":0,\"errmsg\":\"成功\"}", responseString,true);
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
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,true);
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
        JSONAssert.assertEquals("{\"errno\":504,\"errmsg\":\"无该权限\"}", responseString,true);
    }
    @Test
    public void testForbidPrivs() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/0/privileges/21/forbid")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"code\":\"OK\",\"errmsg\":\"成功\",\"data\":null}", responseString,true);
    }
    @Test
    public void testForbidPrivsWithErrorDid() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/1/privileges/21/forbid")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,true);
    }
    @Test
    public void testReleasePrivsWithErrorDid() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/1/privileges/21/release")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,true);
    }
    @Test
    public void testReleasePrivs() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/0/privileges/21/release")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":0,\"errmsg\":\"成功\"}", responseString,true);
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
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}", responseString,true);
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
        JSONAssert.assertEquals("{\"errno\":0,\"errmsg\":\"成功\"}", responseString,true);
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
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,true);
    }

}

