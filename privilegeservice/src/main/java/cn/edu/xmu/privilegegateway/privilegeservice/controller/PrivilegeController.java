/**
 * Copyright School of Informatics Xiamen University
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

package cn.edu.xmu.privilegegateway.privilegeservice.controller;

import cn.edu.xmu.privilegegateway.annotation.aop.Audit;
import cn.edu.xmu.privilegegateway.annotation.aop.Depart;
import cn.edu.xmu.privilegegateway.annotation.aop.LoginName;
import cn.edu.xmu.privilegegateway.annotation.aop.LoginUser;
import cn.edu.xmu.privilegegateway.annotation.model.VoObject;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Privilege;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.User;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.*;
import cn.edu.xmu.privilegegateway.privilegeservice.service.*;
import cn.edu.xmu.privilegegateway.annotation.util.*;
import cn.edu.xmu.privilegegateway.annotation.util.IpUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限控制器
 * @author Ming Qiu
 * Modified at 2020/11/5 13:21
 **/
@Api(value = "权限服务", tags = "privilege")
@RestController /*Restful的Controller对象*/
@RefreshScope
@RequestMapping(value = "", produces = "application/json;charset=UTF-8")
public class PrivilegeController {

    static final Integer IMAGE_MAX_SIZE=1000000;
    private  static  final Logger logger = LoggerFactory.getLogger(PrivilegeController.class);

    private JwtHelper jwtHelper = new JwtHelper();

    @Autowired
    private RoleService roleService;

    @Autowired
    private HttpServletResponse httpServletResponse;

    @Autowired
    private UserService userService;

    @Autowired
    private NewUserService newUserService;

    @Autowired
    private UserProxyService userProxyService;

    @Autowired
    private PrivilegeService privilegeService;

    /**
     * @author: zhang yu
     * @date: 2021/11/24 16:28
     * @version: 1.0
    */
    /*获取权限状态*/

    /**
     *
     * @return
     */
    @ApiOperation(value = "查询权限状态")
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @GetMapping("/privileges/states")
    public Object GetPrivilegesStates()
    {
        ReturnObject<List> ret=privilegeService.getPrivilegeStates();
        return Common.decorateReturnObject(ret);
    }
    /*查询功能角色权限*/

    /**
     * @author zhangyu
     * @param did
     * @param roleid
     * @param page
     * @param pagesize
     * @return
     */
    @ApiOperation(value="查询功能角色权限")
    @ApiResponses({
            @ApiResponse(code = 505, message = "did不为0"),
            @ApiResponse(code=0,message="成功")
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/baseroles/{id}/privileges")
    public Object GetBaseRolePriv(@PathVariable("did") Long did,
                                  @PathVariable("id") Long roleid,
                                 @RequestParam(required = true,value = "page") Integer page,
                                  @RequestParam(required = true,value="pageSize") Integer pagesize)
    {
        if(did!=0)
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        return Common.decorateReturnObject(roleService.selectBaseRolePrivs(roleid,page,pagesize));
    }
    /*取消功能角色权限,删除功能角色权限
    * 因为角色会有取消继承其它功能角色来取消权限，所以单独写一个
    * */
    @ApiOperation(value="取消功能角色权限")
    @ApiResponses({
            @ApiResponse(code = 505, message = "did不为0"),
            @ApiResponse(code=0,message="成功")
    })
    @Audit(departName = "departs")
    @DeleteMapping("/departs/{did}/baseroles/{roleid}/privileges/{privilegeid}")
    public Object delBaseRolePriv(@PathVariable Long did,
                                  @PathVariable Long roleid,
                                  @PathVariable("privilegeid") Long pid)
    {
        if(did!=0)
        {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }
        else
        {
            return Common.decorateReturnObject(roleService.delBaseRolePriv(roleid,pid));
        }
    }
    /*新建权限，新增权限*/

    /**
     * @author: zhangyu
     * @param did
     * @param vo
     * @param bindingResult
     * @return
     */
    @ApiOperation(value="新建权限")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "roleid", value = "角色id", required = true, dataType = "Integer", paramType = "path")
            })
    @ApiResponses({
            @ApiResponse(code = 742, message = "权限url/RequestType重复"),
            @ApiResponse(code=0,message="成功")
    })
    @Audit(departName = "departs")
    @PostMapping("/departs/{did}/privileges")
    public Object AddPriv(
                        @PathVariable("did") Long did,
                        @Valid @RequestBody PrivilegeVo vo,
                          BindingResult bindingResult,
                          @LoginUser Long creatorid,
                        @LoginName String creatorname)
    {
        if(did!=Long.valueOf(0))
        {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }
        ReturnObject<VoObject> returnObject=privilegeService.AddPrivileges(vo,creatorid,creatorname);
        return Common.decorateReturnObject(returnObject);

    }
    /*查询权限*/

    /**
     * @author zhangyu
     * @param did
     * @param url
     * @param requestType
     * @return
     */
    @ApiOperation(value="查询权限")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name = "did", value = "部门id", required = true, dataType = "Integer", paramType = "path"),
            @ApiImplicitParam(name = "url", value = "访问url", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "requestType", value = "请求类型", required = true, dataType = "Integer", paramType = "path")
    })
    @ApiResponses({
            @ApiResponse(code = 505, message = "did不为0"),
            @ApiResponse(code=0,message="成功")
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/privileges")
    public Object getPrivs(@PathVariable Long did,
                           @RequestParam String url,
                           @RequestParam Byte requestType,
                            @RequestParam(required = true,value="page") Integer page,
                           @RequestParam(required = true,value="pageSize") Integer pageSize)
    {
        if(did!=Long.valueOf(0))
        {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }
        return Common.decorateReturnObject(privilegeService.GetPriv(url,requestType,page,pageSize));
    }
    /*删除权限*/

    /**
     * @author zhangyu
     * @param did
     * @param pid
     * @return
     */
    @ApiOperation(value = "删除权限")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="id", value="权限id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="Integer", paramType="path")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 505, message = "操作id不是自己的对象")
    })
    @Audit(departName = "departs")
    @DeleteMapping("/departs/{did}/privileges/{id}")
    public Object DeletePriv(@PathVariable("did") Long did,
                             @PathVariable("id") Long pid)
    {
        if(did!=Long.valueOf(0))
        {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }
        return  Common.decorateReturnObject(privilegeService.DelPriv(pid));
    }
    /*禁用权限*/

    /**
     *
     * @param did
     * @param pid
     * @return
     */
    @ApiOperation("禁用权限")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="id", value="权限id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="Integer", paramType="path")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 505, message = "操作id不是自己的对象")
    })
    @Audit(departName = "departs")
    @PutMapping("/departs/{did}/privileges/{id}/forbid")
    public Object ForbidPriv(@PathVariable("did") Long did,
                             @PathVariable("id") Long pid,
                             @LoginUser Long modifiedId,
                             @LoginName String modifiedName)
    {
        if(did!=Long.valueOf(0))
        {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }
        return Common.decorateReturnObject(privilegeService.ForbidPriv(pid,modifiedId,modifiedName));
    }
    /*解禁权限*/

    /**
     *
     * @param did
     * @param pid
     * @return
     */
    @ApiOperation("解禁权限")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="id", value="权限id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="Integer", paramType="path")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 505, message = "操作id不是自己的对象")
    })
    @Audit(departName = "departs")
    @PutMapping("/departs/{did}/privileges/{id}/release")
    public Object ReleasePriv(@PathVariable("did") Long did,
                             @PathVariable("id") Long pid,
                              @LoginUser Long mid,
                              @LoginName String mname)
    {
        if(did!=Long.valueOf(0))
        {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }
        return Common.decorateReturnObject(privilegeService.ReleasePriv(pid,mid,mname));

    }

    /***
     * 取消用户权限
     * @param userid 用户id
     * @param roleid 角色id
     * @param did 部门id
     * @return
     */
//    @ApiOperation(value = "取消用户角色")
//    @ApiImplicitParams({
//            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
//            @ApiImplicitParam(name="id", value="角色id", required = true, dataType="Integer", paramType="path"),
//            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="Integer", paramType="path")
//
//    })
//    @ApiResponses({
//            @ApiResponse(code = 0, message = "成功"),
//            @ApiResponse(code = 504, message = "操作id不存在")
//    })
//    @Audit(departName = "departs")
//    @DeleteMapping("/departs/{did}/adminusers/{userid}/roles/{roleid}")
//    public Object revokeRole(@PathVariable Long did, @PathVariable Long userid, @PathVariable Long roleid){
//        return Common.decorateReturnObject(userService.revokeRole(userid, roleid, did));
//    }

    /***
     * 赋予用户权限
     * @param userid 用户id
     * @param roleid 角色id
     * @param createid 创建者id
     * @param did 部门id
     * @return
     */
    @ApiOperation(value = "赋予用户角色")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="userid", value="用户id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="roleid", value="角色id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit(departName = "departs")
    @PostMapping("/departs/{did}/adminusers/{userid}/roles/{roleid}")
    public Object assignRole(@LoginUser Long createid, @PathVariable Long did, @PathVariable Long userid, @PathVariable Long roleid){

        ReturnObject<VoObject> returnObject =  userService.assignRole(createid, userid, roleid, did);
        if (returnObject.getCode() == ReturnNo.OK) {
            return Common.getRetObject(returnObject);
        } else {
            return Common.decorateReturnObject(returnObject);
        }

    }

    /***
     * 获得自己角色信息
     * @author Xianwei Wang
     * @return
     */
    @ApiOperation(value = "获得自己角色信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),

    })
    @Audit
    @GetMapping("/adminusers/self/roles")
    public Object getUserSelfRole(@LoginUser Long id){
        ReturnObject<List> returnObject =  userService.getSelfUserRoles(id);
        return Common.getListRetObject(returnObject);
    }

    /***
     * 获得所有人角色信息
     * @param id 用户id
     * @param did 部门id
     * @return
     */
    @ApiOperation(value = "获得所有人角色信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="id", value="用户id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/adminusers/{id}/roles")
    public Object getSelfRole(@PathVariable Long did, @PathVariable Long id){
        ReturnObject<List> returnObject =  userService.getUserRoles(id, did);
        if (returnObject.getCode() == ReturnNo.OK) {
            return Common.getListRetObject(returnObject);
        } else {
            return Common.decorateReturnObject(returnObject);
        }

    }


    /**
     * 获得所有权限
     * @return Object
     * createdBy Ming Qiu 2020/11/03 23:57
     */
    @ApiOperation(value = "获得所有权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @GetMapping("privileges")
    public Object getAllPrivs(@RequestParam(required = false) Integer page, @RequestParam(required = false) Integer pageSize){

        logger.debug("getAllPrivs: page = "+ page +"  pageSize ="+pageSize);

        page = (page == null)?1:page;
        pageSize = (pageSize == null)?10:pageSize;

        logger.debug("getAllPrivs: page = "+ page +"  pageSize ="+pageSize);
        ReturnObject<PageInfo<VoObject>> returnObject =  userService.findAllPrivs(page, pageSize);
        return Common.getPageRetObject(returnObject);
    }

    /**
     * 修改权限
     * @param id : 权限id
     * @return Object
     * createdBy Ming Qiu 2020/11/03 23:57
     */
    @ApiOperation(value = "修改权限信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="String", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @PutMapping("/departs/{did}/privileges/{id}")
    public Object changePriv(@PathVariable("id") Long id,
                             @Validated @RequestBody PrivilegeVo vo,
                             BindingResult bindingResult,
                             @LoginUser Long ModifierId,
                             @LoginName String ModifierName,
                             @PathVariable("did") Long departId){
        /* 处理参数校验错误 */
        Object o = Common.processFieldErrors(bindingResult, httpServletResponse);
        if(o != null){
            return o;
        }
        if (departId !=0){
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }

        ReturnObject<VoObject> returnObject = privilegeService.changePriv(id,vo,ModifierId,ModifierName);
        return Common.decorateReturnObject(returnObject);

    }

    /**
     * auth007: 查询某一用户权限
     * @author yue hao
     * @param id
     * @return Object
     */
    @ApiOperation(value = "获得某一用户的权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="did", required = true, dataType="String", paramType="path"),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit(departName = "departs") // 需要认证
    @GetMapping("/departs/{did}/adminusers/{id}/privileges")
    public Object getPrivsByUserId(@PathVariable Long id, @PathVariable Long did){
        ReturnObject<List> returnObject =  userService.findPrivsByUserId(id,did);
        if (returnObject.getCode() == ReturnNo.OK) {
            return Common.getListRetObject(returnObject);
        } else {
            return Common.decorateReturnObject(returnObject);
        }
    }

    /**
     * @author XQChen
     * @date Created in 2020/11/8 0:33
     **/
    @ApiOperation(value = "查看自己信息",  produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value ="用户token", required = true)
    })
    @ApiResponses({
    })
    @Audit
    @GetMapping(value = "adminusers",produces = "application/json;charset=UTF-8")
    public Object getUserSelf(@LoginUser Long userId) {
        logger.debug("getUserSelf userId:" + userId);

        Object returnObject;

        ReturnObject<VoObject> user =  userService.findUserById(userId);
        logger.debug("finderSelf: user = " + user.getData() + " code = " + user.getCode());

        returnObject = Common.getRetObject(user);

        return returnObject;
    }

    /**
     * @author XQChen
     * @date Created in 2020/11/8 0:33
     **/
    @ApiOperation(value = "查看任意用户信息",  produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String",  name = "authorization", value ="用户token", required = true),
            @ApiImplicitParam(paramType = "path",   dataType = "Integer", name = "id",            value ="用户id",    required = true),
            @ApiImplicitParam(paramType = "path",   dataType = "Integer", name = "did",           value ="店铺id",    required = true)
    })
    @ApiResponses({
    })
    @Audit(departName = "departs")
    @GetMapping(value = "/departs/{did}/adminusers/{id}",produces = "application/json;charset=UTF-8")
    public Object getUserById(@PathVariable("id") Long id, @PathVariable("did") Long did) {

        ReturnObject returnObject = null;

        ReturnObject<VoObject> user = userService.findUserByIdAndDid(id, did);
        logger.debug("findUserByIdAndDid: user = " + user.getData() + " code = " + user.getCode());

        return Common.decorateReturnObject(user);
    }

    /**
     * @author XQChen
     * @date Created in 2020/11/8 0:33
     **/

    @ApiOperation(value = "auth003: 查询用户信息",  produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String",  name = "authorization", value ="用户token", required = true),
            @ApiImplicitParam(paramType = "query",  dataType = "String",  name = "userName",      value ="用户名",    required = false),
            @ApiImplicitParam(paramType = "query",  dataType = "String",  name = "mobile",        value ="电话号码",  required = false),
            @ApiImplicitParam(paramType = "query",  dataType = "Integer", name = "page",          value ="页码",      required = true),
            @ApiImplicitParam(paramType = "query",  dataType = "Integer", name = "pagesize",      value ="每页数目",  required = true),
            @ApiImplicitParam(paramType = "path",   dataType = "Integer", name = "did",            value ="店铺id",    required = true)
    })
    @ApiResponses({
    })
    @Audit(departName = "departs")
    @GetMapping(value = "/departs/{did}/adminusers/all",produces = "application/json;charset=UTF-8")
    public Object findAllUser(
            @RequestParam  String  userName,
            @RequestParam  String  mobile,
            @RequestParam(required = false, defaultValue = "1")  Integer page,
            @RequestParam(required = false, defaultValue = "10")  Integer pagesize,
            @PathVariable("did") Long did) {

        ReturnObject object = null;

        ReturnObject<PageInfo<VoObject>> returnObject = userService.findAllUsers(userName, mobile, page, pagesize, did);
        logger.debug("findUserById: getUsers = " + returnObject);
        object = Common.getPageRetObject(returnObject);


        return Common.decorateReturnObject(object);
    }



    /* auth008 start*/
    //region
    /**
     * 分页查询所有角色
     *
     * @author 24320182203281 王纬策
     * @param page 页数
     * @param pageSize 每页大小
     * @return Object 角色分页查询结果
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2020/11/26 10:55
     */
    @ApiOperation(value = "查询角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "did", value = "部门id", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "页码", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize", value = "每页数目", required = false)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/roles")
    public Object selectAllRoles(@LoginUser Long userId,
                                 @Depart Long departId,
                                 @PathVariable("did") Long did,
                                 @RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer pageSize) {
        logger.debug("selectAllRoles: page = " + page + "  pageSize =" + pageSize);

        ReturnObject retObj = roleService.selectAllRoles(did, page, pageSize);
        return Common.decorateReturnObject(retObj);
    }

    /**
     * 新增一个角色
     *
     * @author 24320182203281 王纬策
     * @param vo 角色视图
     * @param bindingResult 校验错误
     * @param userId 当前用户id
     * @return Object 角色返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:02
     */
    @ApiOperation(value = "新增角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "body", dataType = "RoleVo", name = "vo", value = "可修改的用户信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @PostMapping("/departs/{did}/roles")
    public Object insertRole(@PathVariable("did") Long did,
                             @Validated @RequestBody RoleVo vo,
                             BindingResult bindingResult,
                             @LoginUser Long userId,
                             @LoginName String userName,
                             @Depart Long departId) {
        logger.debug("insert role by userId:" + userId);
        // 校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return Common.decorateReturnObject(new ReturnObject(returnObject));
        }

        Role role = (Role) Common.cloneVo(vo, Role.class);
        role.setDepartId(did);
        role.setBaserole((byte) 0);
        Common.setPoCreatedFields(role, userId, userName);

        ReturnObject retObj = roleService.insertRole(role);
        return Common.decorateReturnObject(retObj);
    }

    /**
     * 新增一个功能角色
     *
     * @author 22920192204289 王文凯
     * @param vo 角色视图
     * @param bindingResult 校验错误
     * @param userId 当前用户id
     * @return Object 角色返回视图
     */
    @ApiOperation(value = "新增功能角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "body", dataType = "RoleVo", name = "vo", value = "可修改的用户信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @PostMapping("/departs/{did}/baseroles")
    public Object insertBaseRole(@PathVariable("did") Long did,
                                 @Validated @RequestBody RoleVo vo,
                                 BindingResult bindingResult,
                                 @LoginUser Long userId,
                                 @LoginName String userName,
                                 @Depart Long departId) {
        logger.debug("insert role by userId:" + userId);
        // 校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return Common.decorateReturnObject(new ReturnObject(returnObject));
        }

        if (did != 0L) {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }

        Role role = (Role) Common.cloneVo(vo, Role.class);
        role.setDepartId(did);
        role.setBaserole((byte)1);
        Common.setPoCreatedFields(role, userId, userName);

        ReturnObject retObj = roleService.insertBaseRole(role);
        return Common.decorateReturnObject(retObj);
    }

    /**
     * 查询功能角色
     *
     * @author 22920192204289 王文凯
     */
    @ApiOperation(value = "查询角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "did", value = "部门id", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "页码", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize", value = "每页数目", required = false)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/roles")
    public Object selectBaseRoles(@LoginUser Long userId,
                                  @Depart Long departId,
                                  @PathVariable("did") Long did,
                                  @RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer pageSize) {
        logger.debug("selectAllRoles: page = " + page + "  pageSize =" + pageSize);

        if (did != 0L) {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }

        ReturnObject retObj = roleService.selectBaseRoles(page, pageSize);
        return Common.decorateReturnObject(retObj);
    }

    /**
     * 删除角色，同时级联删除用户角色表与角色权限表
     *
     * @author 24320182203281 王纬策
     * @param id 角色id
     * @return Object 删除结果
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:15
     */
    @ApiOperation(value = "删除角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "did", value = "部门id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "角色id", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @DeleteMapping("/departs/{did}/roles/{id}")
    public Object deleteRole(@PathVariable("did") Long did,
                             @PathVariable("id") Long id,
                             @LoginUser Long userId,
                             @Depart Long departId) {
        logger.debug("delete role");
        ReturnObject retObj = roleService.deleteRole(id, did);
        return Common.decorateReturnObject(retObj);
    }

    /**
     * 修改角色信息
     *
     * @author 24320182203281 王纬策
     * @param id 角色id
     * @param vo 角色视图
     * @param bindingResult 校验数据
     * @param userId 当前用户id
     * @return Object 角色返回视图
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
     * modifiedBy 王文凯 2021/11/26 11:23
     */
    @ApiOperation(value = "修改角色信息", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "did", value = "部门id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "角色id", required = true),
            @ApiImplicitParam(paramType = "body", dataType = "RoleVo", name = "vo", value = "可修改的用户信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 736, message = "角色名已存在"),
    })
    @Audit(departName = "departs")
    @PutMapping("/departs/{did}/roles/{id}")
    public Object updateRole(@PathVariable("did") Long did,
                             @PathVariable("id") Long id,
                             @Validated @RequestBody RoleVo vo,
                             BindingResult bindingResult,
                             @LoginUser Long userId,
                             @LoginName String userName,
                             @Depart Long departId) {
        logger.debug("update role by userId:" + userId);
        // 校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return Common.decorateReturnObject(new ReturnObject(returnObject));
        }

        Role role = (Role) Common.cloneVo(vo, Role.class);
        role.setId(id);
        role.setDepartId(did);
        role.setGmtModified(LocalDateTime.now());
        Common.setPoModifiedFields(role, userId, userName);

        ReturnObject retObj = roleService.updateRole(role);
        return Common.decorateReturnObject(retObj);
    }

    /**
     * 查看任意用户的角色
     *
     * @author 22920192204289 王文凯
     * @param did 部门id
     * @param id 用户id
     * @param page 页数
     * @param pageSize 每页大小
     * @return Object 角色返回视图
     * createdBy 王文凯 2021/11/26 11:44
     */
    @ApiOperation(value = "查看任意用户的角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "did", value = "部门id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "用户id", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "页码", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize", value = "每页数目", required = false)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/users/{id}/roles")
    public Object selectRoles(@LoginUser Long userId,
                              @Depart Long departId,
                              @PathVariable("did") Long did,
                              @PathVariable("id") Long id,
                              @RequestParam(required = false) Integer page,
                              @RequestParam(required = false) Integer pageSize) {
        logger.debug("selectRoles: page = " + page + "  pageSize =" + pageSize);
        ReturnObject retObj = userService.selectRoles(id, did, page, pageSize);
        return Common.decorateReturnObject(retObj);
    }

    /**
     * 查看自己的角色
     *
     * @author 22920192204289 王文凯
     * @param page 页数
     * @param pageSize 每页大小
     * @return Object 角色返回视图
     * createdBy 王文凯 2021/11/26 11:44
     */
    @ApiOperation(value = "查看自己的角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "页码", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize", value = "每页数目", required = false)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/self/roles")
    public Object selectSelfRoles(@LoginUser Long userId,
                                  @Depart Long departId,
                                  @RequestParam(required = false) Integer page,
                                  @RequestParam(required = false) Integer pageSize) {
        logger.debug("selectSelfRoles: page = " + page + "  pageSize =" + pageSize);
        return Common.decorateReturnObject(userService.selectSelfRoles(userId, departId, page, pageSize));
    }

    /**
     * 查看自己的功能角色
     *
     * @author 22920192204289 王文凯
     * @param page 页数
     * @param pageSize 每页大小
     * @return Object 角色返回视图
     * createdBy 王文凯 2021/11/26 11:44
     */
    @ApiOperation(value = "查看自己的功能角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "页码", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize", value = "每页数目", required = false)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/self/baseroles")
    public Object selectSelfBaseRoles(@LoginUser Long userId,
                                      @Depart Long departId,
                                      @RequestParam(required = false) Integer page,
                                      @RequestParam(required = false) Integer pageSize) {
        logger.debug("selectBaseRoles: page = " + page + "  pageSize =" + pageSize);
        return Common.decorateReturnObject(userService.selectSelfBaseRoles(userId, departId, page, pageSize));
    }

    /**
     * 禁用角色
     *
     * @author 22920192204289 王文凯
     */
    @ApiOperation(value = "禁用角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "did", value = "部门id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "角色id", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @PutMapping("/departs/{did}/roles/{id}/forbid")
    public Object forbidRole(@PathVariable Long did,
                             @PathVariable Long id) {
        logger.debug("forbidRole");

        Role bo = new Role();
        bo.setId(id);
        bo.setDepartId(did);
        bo.setState(Role.State.FORBID.getCode());

        return Common.decorateReturnObject(roleService.forbidRole(bo));
    }

    /**
     * 解禁角色
     *
     * @author 22920192204289 王文凯
     */
    @ApiOperation(value = "解禁角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "did", value = "部门id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "角色id", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @PutMapping("/departs/{did}/roles/{id}/release")
    public Object releaseRole(@PathVariable Long did,
                              @PathVariable Long id) {
        logger.debug("releaseRole");

        Role bo = new Role();
        bo.setId(id);
        bo.setDepartId(did);
        bo.setState(Role.State.NORM.getCode());

        return Common.decorateReturnObject(roleService.releaseRole(bo));
    }

    /**
     * 查询角色中用户
     *
     * @author 22920192204289 王文凯
     */
    @ApiOperation(value = "查询角色中用户", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "did", value = "部门id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "角色id", required = true),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "page", value = "页码", required = false),
            @ApiImplicitParam(paramType = "query", dataType = "int", name = "pageSize", value = "每页数目", required = false)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/roles/{id}/users")
    public Object selectUserByRole(@PathVariable Long did,
                                   @PathVariable Long id,
                                   @PathVariable(required = false) Integer page,
                                   @PathVariable(required = false) Integer pageSize) {
        logger.debug("selectUserByRole");
        return Common.decorateReturnObject(roleService.selectUserByRole(id, did, page, pageSize));
    }

    //endregion
    /* auth008 end*/

    /* auth009 */

    /**
     * auth009: 修改任意用户信息
     * @param id: 用户 id
     * @param vo 修改信息 UserVo 视图
     * @return Object
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:20
     * Modified by 19720182203919 李涵 at 2020/11/8 0:19
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @ApiOperation(value = "修改任意用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs") // 需要认证
    @PutMapping("/departs/{did}/users/{id}")
    public Object modifyUserInfo(@PathVariable Long did,@PathVariable Long id,@RequestBody ModifyUserVo vo,@LoginUser Long loginUser,@LoginName String loginName) {
        logger.debug("modifyUserInfo: id = "+ id +" vo = " + vo);
        ReturnObject returnObj = userService.modifyUserInfo(did,id, vo,loginUser,loginName);
        return Common.decorateReturnObject(returnObj);
    }

    /**
     * auth009: 删除任意用户
     * @param id: 用户 id
     * @return Object
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:20
     * Modified by 19720182203919 李涵 at 2020/11/8 0:19
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @ApiOperation(value = "删除任意用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs") // 需要认证
    @DeleteMapping("/departs/{did}/users/{id}")
    public Object deleteUser(@PathVariable Long did,@PathVariable Long id,@LoginUser Long loginUser,@LoginName String loginName) {
        logger.debug("deleteUser: id = "+ id);
        ReturnObject returnObject = userService.deleteUser(did,id,loginUser,loginName);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * auth009: 禁止用户登录
     * @param id: 用户 id
     * @return Object
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:20
     * Modified by 19720182203919 李涵 at 2020/11/8 0:19
     *Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @ApiOperation(value = "禁止用户登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs") // 需要认证
    @PutMapping("/departs/{did}/users/{id}/forbid")
    public Object forbidUser(@PathVariable Long did,@PathVariable Long id,@LoginUser Long loginUser,@LoginName String loginName) {

        logger.debug("forbidUser: id = "+ id);
        ReturnObject returnObject = userService.forbidUser(did,id,loginUser,loginName);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * auth009: 解禁用户
     * @param id: 用户 id
     * @return Object
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:20
     * Modified by 19720182203919 李涵 at 2020/11/8 0:19
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @ApiOperation(value = "解禁用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs") // 需要认证
    @PutMapping("/departs/{did}/users/{id}/release")
    public Object releaseUser(@PathVariable Long did,@PathVariable Long id,@LoginUser Long loginUser,@LoginName String loginName) {

        logger.debug("releaseUser: id = "+ id);
        ReturnObject returnObject = userService.releaseUser(did,id,loginUser,loginName);
        return Common.decorateReturnObject(returnObject);
    }

    /* auth009 结束 */

    /**
     * 用户登录
     * @param loginVo
     * @param bindingResult
     * @param httpServletResponse
     * @param httpServletRequest
     * @return
     * @author 24320182203266
     */
    @ApiOperation(value = "登录")
    @PostMapping("login")
    public Object login(@Validated @RequestBody LoginVo loginVo, BindingResult bindingResult
            , HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest){
        /* 处理参数校验错误 */
        Object o = Common.processFieldErrors(bindingResult, httpServletResponse);
        if(o != null){
            return o;
        }

        String ip = IpUtil.getIpAddr(httpServletRequest);
        return Common.decorateReturnObject(userService.login(loginVo.getUserName(), loginVo.getPassword(), ip));
    }

    /**
     * 用户注销
     * @param userId
     * @return
     * @author 24320182203266
     */
    @ApiOperation(value = "注销")
    @Audit
    @GetMapping("logout")
    public Object logout(@LoginUser Long userId){

        logger.debug("logout: userId = "+userId);
        return Common.decorateReturnObject(userService.Logout(userId));
    }

    /**
     * 内部api-将某个用户的权限信息装载到Redis中
     * @param id: 用户 id
     * @return Object 装载的用户id
     * @author RenJie Zheng 22920192204334
     */
    @Audit
    @PutMapping("internal/users/{id}/privileges/load")
    public Object loadUserPrivilege(@PathVariable Long id,HttpServletRequest httpServletRequest){
        String jwt = httpServletRequest.getHeader("authorization");
        return Common.decorateReturnObject(userService.loadUserPrivilege(id,jwt));
    }

    /**
     * @param userId
     * @param multipartFile
     * @return
     * @author 24320182203218
     **/
    @ApiOperation(value = "用户上传图片",  produces="application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "formData", dataType = "file", name = "img", value ="文件", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 506, message = "该目录文件夹没有写入的权限"),
            @ApiResponse(code = 508, message = "图片格式不正确"),
            @ApiResponse(code = 509, message = "图片大小超限")
    })
    @Audit
    @PostMapping("/adminusers/uploadImg")
    public Object uploadImg(@RequestParam("img") MultipartFile multipartFile, @LoginUser @ApiIgnore Long userId){
        logger.debug("uploadImg: id = "+ userId +" img :" + multipartFile.getOriginalFilename());
        ReturnObject returnObject = userService.uploadImg(userId,multipartFile);
        return Common.decorateReturnObject(returnObject);
    }


    //====================以下

    /**
     *设置用户代理关系(2021-2-14)
     * @param proxyUserId  被代理用户
     * @param creatorName 创建者
     * @param departId  部门id
     * @param userId    代理用户
     * @param vo        代理时间
     * @param bindingresult
     * @return
     * createdBy Di Han Li 2020/11/04 09:57
     * Modified by 24320182203221 李狄翰 at 2020/11/8 8:00
     * Modified by 22920192204222 郎秀晨 at 2021/11/25
     */
    @Audit(departName = "departs")
    @PostMapping("users/{id}/proxy")
    public Object setUsersProxy(@LoginUser Long proxyUserId, @LoginName String creatorName, @Depart Long departId, @PathVariable("id") Long userId,
                                @Validated @RequestBody UserProxyVo vo, BindingResult bindingresult) {
        Object obj = Common.processFieldErrors(bindingresult, httpServletResponse);
        if (null != obj) {
            return obj;
        }
        if(userId.equals(proxyUserId)){
            return Common.decorateReturnObject(new ReturnObject<>(ReturnNo.USERPROXY_SELF));
        }
        if (vo.getBeginDate().isAfter(vo.getEndDate())) {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.USERPROXY_BIGGER));
        }
        ReturnObject retObject = userProxyService.setUsersProxy(proxyUserId, userId, vo, departId, proxyUserId, creatorName);
        return Common.decorateReturnObject(retObject);
    }

    /**
     * 管理员设置用户代理关系(2021-2-14)
     *
     * @param departId    部门
     * @param userId      代理者id
     * @param proxyUserId 被代理者id
     * @return createdBy Di Han Li 2020/11/04 09:57
     * Modified by 24320182203221 李狄翰 at 2020/11/8 8:00
     * Modified by 22920192204222 郎秀晨 at 2021/11/25
     */
    @Audit(departName = "departs")
    @PostMapping("departs/{did}/users/{aid}/proxyusers/{bid}")
    public Object setUsersProxyByAdmin(@LoginUser Long creatorId,
                                       @LoginName String creatorName,
                                       @PathVariable("did") Long departId,
                                       @PathVariable("aid") Long userId,
                                       @PathVariable("bid") Long proxyUserId,
                                       @Validated @RequestBody UserProxyVo vo, BindingResult bindingresult) {
        if(departId!=0){
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.AUTH_NO_RIGHT));
        }
        Object obj = Common.processFieldErrors(bindingresult, httpServletResponse);
        if (null != obj) {
            return obj;
        }
        if(userId.equals(proxyUserId)){
            return Common.decorateReturnObject(new ReturnObject<>(ReturnNo.USERPROXY_SELF));
        }
        if (vo.getBeginDate().isAfter(vo.getEndDate())) {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.USERPROXY_BIGGER));
        }
        ReturnObject retObject = userProxyService.setUsersProxy(proxyUserId, userId, vo, departId, creatorId, creatorName);
        return Common.decorateReturnObject(retObject);
    }

    /**
     * 解除用户代理关系(2021-2-14)
     *
     * @param id 删除的id
     * @return createdBy Di Han Li 2020/11/04 09:57
     * Modified by 24320182203221 李狄翰 at 2020/11/8 8:00
     * Modified by 22920192204222 郎秀晨 at 2021/11/25
     */
    @Audit(departName = "departs")
    @DeleteMapping("proxies/{id}")
    public Object removeUserProxy(@PathVariable("id") Long id, @LoginUser Long userId) {
        ReturnObject returnObject = userProxyService.removeUserProxy(id, userId);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 查询所有用户代理关系(2021-2-14)
     * @param departId 部门id
     * @param userId   代理者id
     * @param proxyUserId 被代理者id
     * @param page 页数
     * @param pageSize 页大小
     * @return
     * createdBy Di Han Li 2020/11/04 09:57
     * Modified by 24320182203221 李狄翰 at 2020/11/8 8:00
     * Modified by 22920192204222 郎秀晨 at 2021/11/25
     */
    @Audit(departName = "departs")
    @GetMapping("departs/{did}/proxies")
    public Object getProxies(@PathVariable("did") Long departId,
                             @RequestParam(value = "aid", required = false) Long userId,
                             @RequestParam(value = "bid", required = false) Long proxyUserId,
                             @RequestParam(value = "page", required = false) Integer page,
                             @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        ReturnObject<List> returnObject = userProxyService.getProxies(userId, proxyUserId, departId, page, pageSize);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 解除代理关系(2021-2-14)
     * @param departId 部门
     * @param id 主键
     * @return createdBy Di Han Li 2020/11/04 09:57
     * Modified by 24320182203221 李狄翰 at 2020/11/8 8:00
     * Modified by 22920192204222 郎秀晨 at 2021/11/25
     */
    @Audit(departName = "departs")
    @DeleteMapping("departs/{did}/proxies/{id}")
    public Object removeAllProxies(@PathVariable("did") Long departId, @PathVariable("id") Long id) {
        ReturnObject returnObject = userProxyService.removeAllProxies(id, departId);
        return Common.decorateReturnObject(returnObject);
    }
//====================以上
    /**
     * 注册用户
     * @param newUserVo:vo对象
     * @param bindingResult 检查结果
     * @return  Object
     * createdBy: LiangJi3229 2020-11-10 18:41
     * modifiedBy: BingShuai Liu 22920192204245
     */
    @ApiOperation(value="注册用户")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "NewUserVo", name = "vo", value = "newUserInfo", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 732, message = "邮箱已被注册"),
            @ApiResponse(code = 733, message = "电话已被注册"),
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 404, message = "参数不合法")
    })
    @PostMapping("users")
    public Object register(@Validated @RequestBody NewUserVo newUserVo, BindingResult bindingResult){
        var res = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (res != null) {
            return res;
        }
        ReturnObject returnObject=newUserService.newUser(newUserVo);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 查询所有状态
     * @return Object
     * createdBy: LiangJi3229 2020-11-10 18:41
     * modifiedBy: BingShuai Liu 22920192204245
     */
    @ApiOperation(value="获得管理员用户的所有状态")
    @ApiResponses({
            @ApiResponse(code = 0,message = "成功")
    })
    @GetMapping("users/states")
    public Object getAllStates(){
        ReturnObject<List> returnObject=userService.getUserStates();
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * auth004: 修改自己的信息
     * @param vo 修改信息 UserVo 视图
     * @param bindingResult 校验信息
     * @return Object
     * @author 24320182203175 陈晓如
     * Created at 2020/11/11 11:22
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @ApiOperation(value = "修改自己的信息")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "body", dataType = "UserVo", name = "vo", value = "可修改的用户信息", required = true)

    })
    @ApiResponses({
            @ApiResponse(code = 732, message = "邮箱已被注册"),
            @ApiResponse(code = 733, message = "电话已被注册"),
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PutMapping("adminusers")
    public Object changeMyAdminselfInfo(@LoginUser @ApiIgnore @RequestParam(required = false) Long id,
                                        @Validated @RequestBody ModifyUserVo vo, BindingResult bindingResult,@LoginName String loginName) {
        if (logger.isDebugEnabled()) {
            logger.debug("modifyUserInfo: id = "+ id +" vo = " + vo);
        }
        // 校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (returnObject != null) {
            logger.info("incorrect data received while modifySelfInfo id = " + id);
            return returnObject;
        }
        ReturnObject returnObj = userService.modifyUserInfo(id,id, vo,id,loginName);
        return Common.decorateReturnObject(returnObj);
    }


    /**
     * auth002: 用户重置密码
     * @param vo 重置密码对象
     * @param httpServletResponse HttpResponse
     * @param bindingResult 校验信息
     * @return Object
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @ApiOperation(value="用户重置密码")
    @ApiResponses({
            @ApiResponse(code = 745, message = "与系统预留的邮箱不一致"),
            @ApiResponse(code = 0, message = "成功"),
    })
    @PutMapping("self/password/reset")
    @ResponseBody
    public Object resetPassword(@Validated @RequestBody ResetPwdVo vo,BindingResult bindingResult
            , HttpServletResponse httpServletResponse) {
            logger.debug("resetPassword");
        /* 处理参数校验错误 */
        Object o = Common.processFieldErrors(bindingResult, httpServletResponse);
        if(o != null){
            return o;
        }

        ReturnObject returnObject = userService.resetPassword(vo);
        return Common.decorateReturnObject(returnObject);
    }


    /**
     * auth002: 用户修改密码
     * @param vo 修改密码对象
     * @return Object
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @ApiOperation(value="用户修改密码",produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 700, message = "用户名不存在或者密码错误"),
            @ApiResponse(code = 741, message = "不能与旧密码相同"),
            @ApiResponse(code = 0, message = "成功"),
    })
    @PutMapping("/self/password")
    @ResponseBody
    public Object modifyPassword(@RequestBody ModifyPwdVo vo) {

        logger.debug("modifyPassword");

        ReturnObject returnObject = userService.modifyPassword(vo);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 获得角色所有权限
     * @return Object
     * createdBy 王琛 24320182203277
     */
    @ApiOperation(value = "获得角色所有权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="String", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @GetMapping("roles/{id}/privileges")
    public Object getRolePrivs(@PathVariable Long id){
        ReturnObject<List> returnObject = roleService.findRolePrivs(id);

        if (returnObject.getCode() == ReturnNo.OK) {
            return Common.getListRetObject(returnObject);
        } else {
            return Common.decorateReturnObject(returnObject);
        }
    }


    /**
     * 取消功能角色权限
     * @return Object
     * createdBy 王琛 24320182203277
     * modifiedby 张宇
     */
    @ApiOperation(value = "取消功能角色权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="did", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="roleid", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="privilegeid", required = true, dataType="String", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("/departs/{did}/baseroles/{id}/privileges")
    public Object delRolePriv(@PathVariable Long did,
                              @PathVariable Long roleid,
                              @PathVariable Long privilegeid){
        logger.debug("delRolePriv: id = "+ did+roleid+privilegeid);
        if(did!=0)
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        ReturnObject returnObject = roleService.delBaseRolePriv(roleid,privilegeid);
        return Common.decorateReturnObject(returnObject);
    }
    /**
     * 增加功能角色权限，给功能角色新增权限
     * @return Object
     * createdBy 王琛 24320182203277
     * modified by 张宇
     */
    @ApiOperation(value = "新增功能角色权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="did", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="roleid", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="privilegeid", required = true, dataType="String", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "daparts")
    @PostMapping("/departs/{did}/baseroles/{roleid}/privileges/{privilegeid}")
    public Object addRolePriv(@PathVariable("did") Long did,
                              @PathVariable Long roleid,
                              @PathVariable Long privilegeid,
                              @LoginUser Long creatorid,
                              @LoginName String creatorname
                                )
    {
        if(did!=0)
        {
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }
        ReturnObject returnObject = roleService.addBaseRolePriv(roleid,privilegeid,creatorid,creatorname);
        return Common.decorateReturnObject(returnObject);

    }


    /**
     * 已注册用户查看自己信息
     * @param userId
     * @param userName
     * @author BingShuai Liu 22920192204245
     * @return
     */
    @Audit(departName = "departs")
    @GetMapping("self/users")
    public Object showSelfInformation(@LoginUser Long userId,
                                      @LoginName String userName){
        ReturnObject ret =userService.showUserInformation(userId,null);
        return Common.decorateReturnObject(ret);
    }

    /**
     * 已注册用户修改自己的信息
     * @param userId
     * @param userName
     * @param userInformationVo
     * @param bindingResult
     * @author BingShuai Liu 22920192204245
     * @return
     */
    @Audit
    @PutMapping("self/users")
    public Object modifyUserInformation(@LoginUser Long userId,
                                        @LoginName String userName,
                                        @Validated @RequestBody UserInformationVo userInformationVo,
                                        BindingResult bindingResult){
        var res = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (res != null) {
            return res;
        }
        ReturnObject ret =userService.modifyUserInformation(userId,userInformationVo,userId,userName);
        return Common.decorateReturnObject(ret);
    }
    /**
     * 注册用户上传头像
     * @param userId
     * @param userName
     * @param request
     * @author BingShuai Liu 22920192204245
     * @return
     */
    @Audit
    @PostMapping("self/users/uploadImg")
    public Object uploadImg(@LoginUser Long userId,
                            @LoginName String userName,
                            HttpServletRequest request){
        //对输入数据进行合法性判断
        List<MultipartFile> files = ((MultipartHttpServletRequest) request)
                .getFiles("file");
        if(files.size()<=0){
            return Common.decorateReturnObject(new ReturnObject<>(ReturnNo.FIELD_NOTVALID));
        }
        MultipartFile multipartFile=files.get(0);
        //图片超限
        if(multipartFile.getSize()>IMAGE_MAX_SIZE){
            return Common.decorateReturnObject(new ReturnObject<>(ReturnNo.IMG_SIZE_EXCEED));
        }

        ReturnObject returnObject=userService.uploadNewImg(userId,multipartFile);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 查询用户信息
     * @param did
     * @param userId
     * @param loginUserName
     * @param userName
     * @param mobile
     * @param email
     * @param page
     * @param pageSize
     * @return
     * @author Bingshuai Liu 22920192204245
     */
    @Audit(departName = "departs")
    @GetMapping("departs/{did}/users")
    public Object showUsers(@PathVariable Long did,
                            @LoginUser Long userId,
                            @LoginName String loginUserName,
                            @RequestParam(required = false) String userName,
                            @RequestParam(required = false) String mobile,
                            @RequestParam(required = false) String email,
                            @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pageSize){
        ReturnObject ret = userService.showUsers(did,userName,mobile,email,page,pageSize);
        if(ret.getCode()!=ReturnNo.OK){
            return Common.decorateReturnObject(ret);
        }
        return Common.getPageRetVo(ret, UserRetVo.class);
    }

    /**
     * 查询新注册用户信息
     * @param did
     * @param userId
     * @param loginUserName
     * @param userName
     * @param mobile
     * @param email
     * @param page
     * @param pageSize
     * @author BingShuai Liu 22920192204245
     * @return
     */
    @Audit(departName = "departs")
    @GetMapping("departs/{did}/users/new")
    public Object showNewUsers(@PathVariable Long did,
                               @LoginUser Long userId,
                               @LoginName String loginUserName,
                               @RequestParam(required = false) String userName,
                               @RequestParam(required = false) String mobile,
                               @RequestParam(required = false) String email,
                               @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer pageSize){
        ReturnObject ret = newUserService.showNewUsers(did,userName,mobile,email,page,pageSize);
        if(ret.getCode()!=ReturnNo.OK){
            return Common.decorateReturnObject(ret);
        }
        return Common.getPageRetVo(ret, UserSimpleRetVo.class);
    }




    /**
     * auth014: 管理员审核用户
     * @param id: 用户 id
     * @return Object
     * @author 24320182203227 LiZihan
     * Modified by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @ApiOperation(value = "管理员审核用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="approve", required = true, dataType="Boolean", paramType="body")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit(departName = "departs") // 需要认证
    @PutMapping("/departs/{did}/users/{id}/approve")
    public Object approveUser(@PathVariable Long did,@PathVariable Long id,@RequestBody ApproveConclusionVo vo,@LoginUser Long loginUser,@LoginName String loginName) {


        logger.debug("approveUser: did = "+ did+" userid: id = "+ id+" opinion: "+vo);

        ReturnObject returnObj = newUserService.approveUser(vo,did,id,loginUser,loginName);;
        return Common.decorateReturnObject(returnObj);
    }


    /**
     * auth014: 将某个用户加入部门
     * Created by 22920192204219 蒋欣雨 at 2021/11/29
     */
    @ApiOperation(value = "将用户加入部门")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", required = true, dataType="Integer", paramType="path"),

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit(departName = "departs") // 需要认证
    @PutMapping("/internal/users/{id}/departs/{did}")
    public Object addToDepart(@PathVariable Long id,@PathVariable Long did,@LoginUser Long loginUser,@LoginName String loginName) {
        if(did!=0)
        {
            return new InternalReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE.getCode(),ReturnNo.RESOURCE_ID_OUTSCOPE.getMessage());
        }
        InternalReturnObject returnObj = userService.addToDepart(did,id,loginUser,loginName);;
        return returnObj;
    }

    /**
     * 查看任意用户信息
     * @param did
     * @param id
     * @param userId
     * @param loginUserName
     * @author BingShuai Liu 22920192204245
     * @return
     */
    @Audit(departName = "departs")
    @GetMapping("departs/{did}/users/{id}")
    public Object showAnyUser(@PathVariable Long did,
                              @PathVariable Long id,
                              @LoginUser Long userId,
                              @LoginName String loginUserName){
        ReturnObject ret =userService.showUserInformation(id,did);
        return Common.decorateReturnObject(ret);
    }

    /**
     * 内部api-获得用户名
     * @param id
     * @param userId
     * @param loginUserName
     * @author BingShuai Liu 22920192204245
     * @return
     */
    @Audit(departName = "departs")
    @GetMapping("internal/users/{id}")
    public Object getUserName(@PathVariable Long id,
                              @LoginUser Long userId,
                              @LoginName String loginUserName){
        ReturnObject ret =userService.showUserName(id);
        return Common.decorateReturnObject(ret);
    }
    @Audit
    @PutMapping("internal/privileges/load")
    public Object loadPrivilege(@Validated @RequestBody PrivilegeRedisVo privilegeVo){
        return Common.decorateReturnObject(privilegeService.loadPrivilege(privilegeVo));
    }

    /**
     * 获得角色的所有状态
     * @author 张晖婧 22920192204320
     * @return Object
     */
    @ApiOperation(value="获得角色的所有状态")
    @Audit
    @GetMapping("/roles/states")
    public Object getRoleAllStates() {
        return Common.decorateReturnObject(roleService.getAllStates());
    }

    /**
     * 获得用户的功能角色
     * @author 张晖婧 22920192204320
     * @param did: 部门 id
     * @param id: 用户 id
     * @return Object
     */
    @ApiOperation(value = "获得用户的功能角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="did", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="id", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="page", required = true, dataType="Integer", paramType="query"),
            @ApiImplicitParam(name="pageSize", required = true, dataType="Integer", paramType="query")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")       //需要加一个部门id不存在
    })
    @Audit(departName = "departs") // 需要认证
    @GetMapping("/departs/{did}/users/{id}/baseroles")
    public Object getBaserolesByUserId(@LoginUser Long userId,
                                       @Depart Long departId,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer pageSize,
                                       @PathVariable Long did,
                                       @PathVariable Long id) {
        ReturnObject ret = userService.findBaserolesByUserId(did, id, page, pageSize);
        return Common.decorateReturnObject(ret);
    }

    /**
     * 查询角色的功能角色
     * @author 张晖婧 22920192204320
     * @param did: 部门 id
     * @param id: 用户 id
     * @return Object
     */
    @ApiOperation(value = "查询角色的功能角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="did", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="id", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="page", required = true, dataType="Integer", paramType="query"),
            @ApiImplicitParam(name="pageSize", required = true, dataType="Integer", paramType="query")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")       //需要加一个部门id不存在
    })
    @Audit(departName = "departs") // 需要认证
    @GetMapping("/departs/{did}/roles/{id}/baseroles")
    public Object getBaserolesByRoleId(@RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer pageSize,
                                       @PathVariable Long did,
                                       @PathVariable Long id) {

        ReturnObject ret = roleService.findBaserolesByRoleId(did, id, page, pageSize);
        return Common.decorateReturnObject(ret);
    }

    /**
     * 设置角色的继承关系
     * @param pid 父角色id
     * @param cid 子角色id
     * @param createId 创建者id
     * @param departId 创建者部门id
     * @param did 部门id
     * @author 22920192204320 张晖婧
     */
    @ApiOperation(value = "设置角色的继承关系")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="pid", value="父角色id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="cid", value="子角色id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit(departName = "departs")
    @PostMapping("/departs/{did}/roles/{pid}/childroles/{cid}")
    public Object createRoleInherited(@LoginUser Long createId,
                                      @Depart Long departId,
                                      @LoginName String createName,
                                      @PathVariable Long did,
                                      @PathVariable Long pid,
                                      @PathVariable Long cid) {
        ReturnObject<VoObject> returnObject = roleService.createRoleInherited(createId,createName,did, pid, cid);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 赋予用户角色
     * @author 张晖婧 22920192204320
     * @param userid 用户id
     * @param roleid 角色id
     * @param createId 创建者id
     * @param did 部门id
     * @return
     */
    @ApiOperation(value = "赋予用户角色")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="userid", value="用户id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="roleid", value="角色id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit(departName = "departs")
    @PostMapping("/departs/{did}/users/{userid}/roles/{roleid}")
    public Object assignRole(@LoginUser Long createId,
                             @LoginName String createName,
                             @PathVariable Long did,
                             @PathVariable Long userid,
                             @PathVariable Long roleid){
        ReturnObject<VoObject> returnObject =  userService.assignRole(createId,createName, userid, roleid, did);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 取消用户角色
     * @author 张晖婧 22920192204320
     * @param userid 用户id
     * @param roleid 角色id
     * @param did 部门id
     * @return
     */
    @ApiOperation(value = "取消用户角色")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="id", value="角色id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="roleid", value="角色id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit(departName = "departs")
    @DeleteMapping("/departs/{did}/users/{userid}/roles/{roleid}")
    public Object revokeRole(@PathVariable Long did,
                             @PathVariable Long userid,
                             @PathVariable Long roleid) {
        return Common.decorateReturnObject(userService.revokeRole(userid, roleid, did));
    }

    /**
     * 查询父角色
     * @author 张晖婧 22920192204320
     * @param did: 部门 id
     * @param id: 角色 id
     * @return Object
     */
    @ApiOperation(value = "查询父角色")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="did", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="id", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="page", required = true, dataType="Integer", paramType="query"),
            @ApiImplicitParam(name="pageSize", required = true, dataType="Integer", paramType="query")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")       //需要加一个部门id不存在
    })
    @Audit(departName = "departs") // 需要认证
    @GetMapping("/departs/{did}/roles/{id}/parents")
    public Object getParentRoles(@RequestParam(required = false) Integer page,
                                 @RequestParam(required = false) Integer pageSize,
                                 @PathVariable Long did,
                                 @PathVariable Long id) {

        ReturnObject ret = roleService.findParentRoles(did, id, page, pageSize);
        return Common.decorateReturnObject(ret);
    }
}
