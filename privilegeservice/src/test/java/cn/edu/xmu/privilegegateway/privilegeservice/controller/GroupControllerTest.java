package cn.edu.xmu.privilegegateway.privilegeservice.controller;

import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import cn.edu.xmu.privilegegateway.privilegeservice.PrivilegeServiceApplication;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.Charset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@AutoConfigureMockMvc
@SpringBootTest(classes = PrivilegeServiceApplication.class)
class GroupControllerTest {
    @Autowired
    private MockMvc mvc;

    private static JwtHelper jwtHelper=new JwtHelper();

    final Charset charset=Charset.forName("UTF-8");
    private static String adminToken = jwtHelper.createToken(1L, "admin", 0L, 1, 3600);
    private static String testToken = jwtHelper.createToken(47L, "user", 0L, 1, 3600);

    /**
     * 获得所有用户组状态
     */

    @Test
    public void getallState() throws Exception {
        String responseString = this.mvc.perform(get("/groups/states"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"data\":[{\"code\":0,\"name\":\"正常\"},{\"code\":1,\"name\":\"禁用\"}],\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }
    /**
     * 获得该部门的所有用户组
     */

    @Test
    @Transactional
    public void getallGroups() throws Exception {

        String responseString = this.mvc.perform(get("/departs/0/groups").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    /**
     * 获得该用户组的所有用户
     */

    @Test
    @Transactional
    public void getallGroupsuser() throws Exception {

        String responseString = this.mvc.perform(get("/departs/0/groups/2/users").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    /**
     * 获得该用户的所有用户组
     */

    @Test
    @Transactional
    public void getallUsersGroup() throws Exception {

        String responseString = this.mvc.perform(get("/departs/0/users/1/groups").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 成功新增用户组
     */
    @Test
    @Transactional
    public void addgroup() throws Exception{

        String requestJson = "{\"name\": \"测试1\"}";
        String responseString = this.mvc.perform(post("/departs/0/groups").contentType("application/json;charset=UTF-8").header("authorization", adminToken).content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }



    @Test
    @Transactional
    public void addgroup_wrongdid1() throws Exception{

        String requestJson = "{\"name\": \"测试1\"}";
        String responseString = this.mvc.perform(post("/departs/2/groups").contentType("application/json;charset=UTF-8").header("authorization", adminToken).content(requestJson))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 修改用户组信息
     * @throws Exception
     */
    @Test
    @Transactional
    public void updategroup() throws Exception{

        String requestJson = "{\"name\": \"测试1\"}";
        String responseString = this.mvc.perform(put("/departs/0/groups/1").contentType("application/json;charset=UTF-8").header("authorization", adminToken).content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    /**
     * 修改用户组信息,组号不存在
     * @throws Exception
     */
    @Test
    @Transactional
    public void updategroup_wrongid1() throws Exception{

        String requestJson = "{\"name\": \"测试1\"}";
        String responseString = this.mvc.perform(put("/departs/0/groups/20").contentType("application/json;charset=UTF-8").header("authorization", adminToken).content(requestJson))
                .andExpect(status().is5xxServerError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":500,\"errmsg\":\"服务器内部错误\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    /**
     * 删除用户组
     * @throws Exception
     */
    @Test
    @Transactional
    public void deletegroup() throws Exception{
        String responseString = this.mvc.perform(delete("/departs/0/groups/1").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }
    /**
     * 删除用户组，用户组id不存在
     * @throws Exception
     */
    @Test
    @Transactional
    public void deletegroup_wrongid1() throws Exception{
        String responseString = this.mvc.perform(delete("/departs/0/groups/11").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":504,\"errmsg\":\"该用户组不存在\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }
    /**
     * 删除用户组，用户组不在部门内
     * @throws Exception
     */
    @Test
    @Transactional
    public void deletegroup_wrongdid() throws Exception{
        String responseString = this.mvc.perform(delete("/departs/1/groups/2").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }



    /**
     * 成功增加用户组关系
     */
    @Test
    @Transactional
    public void addgroupRelation() throws Exception{

        String responseString = this.mvc.perform(post("/departs/0/groups/1/subgroups/3").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 增加用户组关系 冲突
     */
    @Test
    @Transactional
    public void addgroupRelation_wrong() throws Exception{

        String responseString = this.mvc.perform(post("/departs/0/groups/8/subgroups/4").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":735,\"errmsg\":\"该pid与sid的父子关系已经存在\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }



    /**
     * 成功删除用户组关系
     */
    @Test
    @Transactional
    public void deletegroupRelation() throws Exception{

        String responseString = this.mvc.perform(delete("/departs/0/groups/4/subgroups/8").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 删除用户组关系 冲突
     */
    @Test
    @Transactional
    public void deletegroupRelation_wrong() throws Exception{

        String responseString = this.mvc.perform(delete("/departs/0/groups/2/subgroups/1").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":504,\"errmsg\":\"该pid与sid的父子关系不存在\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }


    /**
     * 获得用户组的所有子用户组
     */

    @Test
    public void getallSubGroup() throws Exception {
        String responseString = this.mvc.perform(get("/departs/0/groups/4/subgroups").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    @Test
    public void getallSubGroup1() throws Exception {
        String responseString = this.mvc.perform(get("/departs/0/groups/1/subgroups").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    /**
     * 获得用户组的所有父用户组
     */

    @Test
    public void getallParGroup() throws Exception {
        String responseString = this.mvc.perform(get("/departs/0/groups/8/parents").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    @Test
    public void getallParGroup1() throws Exception {
        String responseString = this.mvc.perform(get("/departs/0/groups/2/parents").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }



    /**
     * 禁用用户组
     * @throws Exception
     */
    @Test
    @Transactional
    public void forbidgroup() throws Exception{

        String responseString = this.mvc.perform(put("/departs/0/group/1/forbid").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    /**
     * 禁用用户组
     * @throws Exception
     */
    @Test
    @Transactional
    public void releasegroup() throws Exception{

        String responseString = this.mvc.perform(put("/departs/0/groups/1/release").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    /**
     * 禁用用户组
     * @throws Exception
     */
    @Test
    @Transactional
    public void releasegroupnone() throws Exception{

        String responseString = this.mvc.perform(put("/departs/0/groups/20/release").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    /**
     * 禁用用户组
     * @throws Exception
     */
    @Test
    @Transactional
    public void releasegroupdid() throws Exception{

        String responseString = this.mvc.perform(put("/departs/2/groups/1/release").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 禁用用户组 部门错误
     * @throws Exception
     */
    @Test
    @Transactional
    public void didwrongforbidgroup() throws Exception{
        String responseString = this.mvc.perform(put("/departs/1/group/1/forbid").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }

    /**
     * 解禁用户组
     * @throws Exception
     */
    @Test
    @Transactional
    public void normalgroup() throws Exception{

        String responseString = this.mvc.perform(put("/departs/0/groups/1/release").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 解禁用户组 部门错误
     * @throws Exception
     */
    @Test
    @Transactional
    public void didwrongnormalgroup() throws Exception{
        String responseString = this.mvc.perform(put("/departs/1/groups/1/release").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, true);
    }
    /**
     * 将用户加入组
     */
    @Test
    @Transactional
    public void addusergroup() throws Exception{

        String responseString = this.mvc.perform(post("/departs/0/groups/1/users/1").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }


    /**
     * 增加用户组关系 不存在
     */
    @Test
    @Transactional
    public void addusergroup_pssame() throws Exception{

        String responseString = this.mvc.perform(post("/departs/0/groups/100/users/1").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":504,\"errmsg\":\"该组不存在\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
        responseString = this.mvc.perform(post("/departs/0/groups/1/users/100").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        expectString = "{\"errno\":504,\"errmsg\":\"操作的资源id不存在\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }
    /**
     * 增加用户组关系 不在部门
     */
    @Test
    @Transactional
    public void addusergroup_departwrong() throws Exception{

        String responseString = this.mvc.perform(post("/departs/1/groups/1/users/1").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, false);

    }

    /**
     * 将用户剔除组
     */
    @Test
    @Transactional
    public void deleteusergroup() throws Exception{

        String responseString = this.mvc.perform(delete("/departs/1/groups/4/users/17330").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":0,\"errmsg\":\"成功\"}";
        JSONAssert.assertEquals(expectString, responseString, false);
    }

    /**
     * 增加用户组关系 不在部门
     */
    @Test
    @Transactional
    public void deleteusergroup_departwrong() throws Exception{

        String responseString = this.mvc.perform(delete("/departs/1/groups/1/users/1").contentType("application/json;charset=UTF-8").header("authorization", adminToken))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType("application/json;charset=UTF-8"))
                .andReturn().getResponse().getContentAsString();
        String expectString = "{\"errno\":505,\"errmsg\":\"操作的资源id不是自己的对象\"}";
        JSONAssert.assertEquals(expectString, responseString, false);

    }
}