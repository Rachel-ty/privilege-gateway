package cn.edu.xmu.privilegegateway.privilegeservice.controller;

import cn.edu.xmu.privilegegateway.annotation.util.JacksonUtil;
import cn.edu.xmu.privilegegateway.annotation.util.JwtHelper;
import cn.edu.xmu.privilegegateway.annotation.util.RedisUtil;
import cn.edu.xmu.privilegegateway.privilegeservice.PrivilegeServiceApplication;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.GroupDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.PrivilegeDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.RoleDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import cn.edu.xmu.privilegegateway.privilegeservice.model.po.UserPo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.LoginVo;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.ModifyUserVo;
import org.apache.http.entity.ContentType;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author zihan zhou
 * @date 2021/12/6
 */
@AutoConfigureMockMvc
@Transactional
@SpringBootTest(classes = PrivilegeServiceApplication.class)
public class ImpactTest {
    private static String token;
    private static String pToken;
    private static String adminToken;
    private static JwtHelper jwtHelper = new JwtHelper();
    public final static String ROLEKEY = "r_%d";
    @Autowired
    private RoleDao roleDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    GroupDao groupDao;
    @Autowired
    PrivilegeDao privilegeDao;
    public final static String GROUPKEY="g_%d";

    private final static String USERKEY = "u_%d";

    @BeforeEach
    void init() {
        token = jwtHelper.createToken(46L, "个", 0L, 1, 36000);
        pToken = jwtHelper.createToken(60L, "pikaas", 0L, 1, 36000);
        adminToken = jwtHelper.createToken(1L, "13088admin", 0L, 1, 36000);
    }
    @Test
    public void privilegeImpactTest() throws JSONException {

        List list=new ArrayList();
        list.add(String.format(USERKEY,60L));
        list.add(String.format(ROLEKEY,2L));
        list.add(String.format(GROUPKEY,10L));
        list.add(String.format(ROLEKEY,3L));
        list.add(String.format(ROLEKEY,6L));
        list.add(String.format(GROUPKEY,12L));
        list.add(String.format(USERKEY,49L));
        list.add(String.format(ROLEKEY,5L));
        list.add(String.format(ROLEKEY,23L));
        String except="[r_2, r_1, u_1, r_4, r_3, r_6, r_5, r_8, r_7, r_9, u_51, u_52, u_50, u_48, r_11, r_106, u_49, r_107, u_46, r_89, u_47, u_55, u_53]";
        String result=privilegeDao.privilegeImpact(94L).toString();
        System.out.println(result);
        JSONAssert.assertEquals(result, except,false);
    }
    //不存在的privilegeId
    @Test
    public void privilegeImpactTest2() throws JSONException {

        String except="[]";
        String result=((HashSet)privilegeDao.privilegeImpact(31223L)).toString();
        JSONAssert.assertEquals(result, except,false);
    }
    @Test
    public void roleImp() throws JSONException {
        List list=new ArrayList();
        list.add(String.format(USERKEY,60L));
        list.add(String.format(ROLEKEY,2L));
        list.add(String.format(GROUPKEY,10L));
        list.add(String.format(ROLEKEY,3L));
        list.add(String.format(ROLEKEY,6L));
        list.add(String.format(GROUPKEY,12L));
        list.add(String.format(USERKEY,49L));
        list.add(String.format(ROLEKEY,5L));
        list.add(String.format(ROLEKEY,23L));
        String except="[u_17332, u_17335, u_17337, u_51, u_52, u_50, r_106, r_107, u_55, u_53, g_6, g_8, r_2, u_17346, r_1, u_1, r_4, r_3, u_17347, r_6, r_5, r_8, r_7, r_9, r_91, u_48, r_11, u_49, u_46, u_47]";
        String result=roleDao.roleImpact(91L).toString();
        System.out.println(result);
        JSONAssert.assertEquals(result, except,false);
    }

    @Test
    public void deleteUserRedis() throws Exception{
        List<String> userResults = (List<String>) userDao.userImpact(17332L);
        List<String> userExpectResults = new ArrayList<>();
        userExpectResults.add(String.format(USERKEY,17337L));
        userExpectResults.add(String.format(USERKEY,17332L));
        JSONAssert.assertEquals(userExpectResults.toString(), userResults.toString(),false);
    }


    @Test
    public void deleteGroupRelationRedis() throws Exception{
        List<String> userResults = (List<String>) groupDao.groupImpact(6L);
        List<String> userExpectResults = new ArrayList<>();
        userExpectResults.add(String.format(GROUPKEY,6L));
        userExpectResults.add(String.format(USERKEY,17332L));
        userExpectResults.add(String.format(USERKEY,17337L));
        userExpectResults.add(String.format(GROUPKEY,8L));
        userExpectResults.add(String.format(USERKEY,17335L));
        JSONAssert.assertEquals(userExpectResults.toString(), userResults.toString(),false);
    }
}