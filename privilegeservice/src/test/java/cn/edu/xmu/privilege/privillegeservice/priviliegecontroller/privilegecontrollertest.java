package cn.edu.xmu.privilege.privillegeservice.priviliegecontroller;

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
@TestPropertySource("classpath:application.yaml")
public class privilegecontrollertest {
    private static String token;
    private static JwtHelper jwtHelper = new JwtHelper();
    @Autowired
    private MockMvc mvc;
    @BeforeEach
    void init() {
        token = jwtHelper.createToken(29L, "zy", 0L, 1, 36000);
    }

    /**
     * 获取权限状态
     */
    @Test
    @Transactional
    public void testGetAllPrivileges() throws Exception{
        String responseString = this.mvc.perform(get("/privileges/states").contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("{\"errno\":0,\"data\":[{\"code\":0,\"name\":\"GET\"},{\"code\":1,\"name\":\"POST\"},{\"code\":2,\"name\":\"PUT\"},{\"code\":3,\"name\":\"DELETE\"}],\"errmsg\":\"成功\"}", responseString);
    }
    /*添加功能角色权限*/
    @Test
    @Transactional
    //错误did
    public void testAddRolePrivilegesByErrordid() throws Exception
    {
        //非功能角色
        String responseString = this.mvc.perform(post("/departs/1/roles/0/privileges/0").contentType("application/json;charset=UTF-8"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString);
    }
    /*添加功能角色权限*/
    @Test
    @Transactional
    /*正确测试*/
    public void testAddRolePrivileges() throws Exception{
        String responseString = this.mvc.perform(post("/departs/0/roles/80/privileges/2").contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("{\"code\":\"OK\",\"errmsg\":\"成功\",\"data\":null}", responseString);
    }
    /*删除功能角色权限*/
    @Test
    @Transactional
    public void testDelPrivsByErrordid() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/1/roles/23/privileges/2").contentType("application/json;charset=UTF-8"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString);
    }
    /*删除功能角色权限*/
    @Test
    @Transactional
    public void testDelBaseRolePrivs() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/0/roles/23/privileges/2").contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertEquals(" {\"errno\":0,\"errmsg\":\"成功\"}", responseString);
    }
    /*查询权限*/
    @Test
    @Transactional
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
        String exstr="{\"errno\":0,\"data\":{\"total\":1,\"list\":[{\"id\":2,\"name\":\"查看任意用户信息\",\"url\":\"/departs/{id}/adminusers/{id}\",\"requestType\":0,\"gmtCreate\":\"2020-11-01 09:52:20.000\",\"gmtModified\":\"2020-11-02 21:51:45.000\",\"creator\":{\"id\":1,\"name\":null},\"modifier\":{\"id\":null,\"name\":null}}],\"pageNum\":1,\"pageSize\":1,\"size\":1,\"startRow\":0,\"endRow\":0,\"pages\":1,\"prePage\":0,\"nextPage\":0,\"isFirstPage\":true,\"isLastPage\":true,\"hasPreviousPage\":false,\"hasNextPage\":false,\"navigatePages\":8,\"navigatepageNums\":[1],\"navigateFirstPage\":1,\"navigateLastPage\":1},\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(exstr, responseString,true);
    }
    /*查询权限 错误did*/
    @Test
    @Transactional
    public void testSelectPrivsWithErrorDid() throws  Exception
    {
        String responseString = this.mvc.perform(get("/departs/1/baseroles/23/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .param("page","1")
                .param("pageSize","10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,true);
    }
    /*查询权限 错误baserole*/
    @Test
    @Transactional
    public void testSelectPrivsWithErrorRid() throws  Exception
    {
        String responseString = this.mvc.perform(get("/departs/0/baseroles/0/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .param("page","1")
                .param("pageSize","10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":0,\"errmsg\":\"成功\"}", responseString,true);
    }
    /*新增权限*/
    @Test
    @Transactional
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
        assertEquals("{\"errno\":0,\"data\":{\"id\":126,\"name\":\"string\",\"url\":\"string\",\"requestType\":0,\"gmtCreate\":null,\"gmtModified\":null,\"creator\":{\"id\":0,\"name\":null},\"modifier\":{\"id\":null,\"name\":null}},\"errmsg\":\"成功\"}", responseString);
    }
    /*新增权限 错误did*/
    @Test
    @Transactional
    public void testAddPrivsWithErrorDid() throws  Exception
    {
        String content="{\"name\": \"string\",\"url\": \"string\",\"requestType\": 0}";
        String responseString = this.mvc.perform(post("/departs/0/privileges")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(content))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString);
    }
    /*新增权限 重复url*/
    @Test
    @Transactional
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
    @Transactional
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
    @Transactional
    public void testDelPrivsWithDid() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/1/privileges/2")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,true);
    }
    /*删除权限,错误pid*/
    @Test
    @Transactional
    public void testDelPrivsWithErrorPid() throws  Exception
    {
        String responseString = this.mvc.perform(delete("/departs/0/privileges/0")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":504,\"errmsg\":\"无该权限\"}", responseString,true);
    }
    @Test
    @Transactional
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
    @Transactional
    public void testForbidPrivsWithErrorDid() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/1/privileges/21/forbid")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,true);
    }
    @Test
    @Transactional
    public void testReleasePrivsWithErrorDid() throws  Exception
    {
        String responseString = this.mvc.perform(put("/departs/1/privileges/21/release")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}", responseString,true);
    }
    @Test
    @Transactional
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
    @Transactional
    /*修改权限信息，错误pid*/
    public void testModifyPrivWithErrorPid()throws Exception
    {
        String body="{\"name\": \"string\",\"url\": \"string\", \"requestType\": 0}";
        String responseString = this.mvc.perform(put("/departs/0/privileges/23")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}", responseString,true);
    }
    @Test
    @Transactional
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
    @Transactional
    public void testModifyPrivWithErrorDid()throws Exception
    {
        String body="{\"name\": \"string\",\"url\": \"string\", \"requestType\": 0}";
        String responseString = this.mvc.perform(put("/departs/1/privileges/23")
                .header("authorization", token)
                .contentType("application/json;charset=UTF-8")
                .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        JSONAssert.assertEquals("{\"errno\":0,\"errmsg\":\"成功\"}", responseString,true);
    }

}
