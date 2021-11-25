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
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Role;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.User;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.*;
import cn.edu.xmu.privilegegateway.privilegeservice.service.NewUserService;
import cn.edu.xmu.privilegegateway.privilegeservice.service.RoleService;
import cn.edu.xmu.privilegegateway.privilegeservice.service.UserProxyService;
import cn.edu.xmu.privilegegateway.privilegeservice.service.UserService;
import cn.edu.xmu.privilegegateway.annotation.util.*;
import cn.edu.xmu.privilegegateway.annotation.util.IpUtil;
import com.github.pagehelper.PageInfo;
import io.swagger.annotations.*;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@RequestMapping(value = "", produces = "application/json;charset=UTF-8")
public class PrivilegeController {

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

    /***
     * 取消用户权限
     * @param userid 用户id
     * @param roleid 角色id
     * @param did 部门id
     * @return
     */
    @ApiOperation(value = "取消用户角色")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="id", value="角色id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="Integer", paramType="path")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 504, message = "操作id不存在")
    })
    @Audit(departName = "departs")
    @DeleteMapping("/departs/{did}/adminusers/{userid}/roles/{roleid}")
    public Object revokeRole(@PathVariable Long did, @PathVariable Long userid, @PathVariable Long roleid){
        return Common.decorateReturnObject(userService.revokeRole(userid, roleid, did));
    }

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
    @PutMapping("/departs/did/privileges/{id}")
    public Object changePriv(@PathVariable Long id, @Validated @RequestBody PrivilegeVo vo, BindingResult bindingResult, @LoginUser Long userId, @Depart Long departId,
                             HttpServletResponse httpServletResponse){
        logger.debug("changePriv: id = "+ id +" vo" + vo);
        logger.debug("getAllPrivs: userId = " + userId +" departId = "+departId);
        /* 处理参数校验错误 */
        Object o = Common.processFieldErrors(bindingResult, httpServletResponse);
        if(o != null){
            return o;
        }
        if (departId !=0){
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        }

        ReturnObject<VoObject> returnObject = userService.changePriv(id, vo);

        if (returnObject.getCode() == ReturnNo.OK) {
            return Common.getRetObject(returnObject);
        } else {
            return Common.decorateReturnObject(returnObject);
        }
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
    public Object selectAllRoles(@LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                 @Depart @ApiIgnore @RequestParam(required = false) Long departId,
                                 @PathVariable("did") Long did,
                                 @RequestParam(required = false, defaultValue = "1") Integer page,
                                 @RequestParam(required = false, defaultValue = "10") Integer pageSize) {
        logger.debug("selectAllRoles: page = "+ page +"  pageSize ="+pageSize);
        if(did.equals(departId)){
            ReturnObject<PageInfo<VoObject>> returnObject =  roleService.selectAllRoles(departId, page, pageSize);
            return Common.getPageRetObject(returnObject);
        }
        else{
            return Common.decorateReturnObject(new ReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE, String.format("部门id不匹配：" + did)));
        }
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
     */
    @ApiOperation(value = "新增角色", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "body", dataType = "RoleVo", name = "vo", value = "可修改的用户信息", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 736, message = "角色名已存在"),
    })
    @Audit(departName = "departs")
    @PostMapping("/departs/{did}/roles")
    public Object insertRole(@PathVariable("did") Long did,
                             @Validated @RequestBody RoleVo vo, BindingResult bindingResult,
                             @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                             @Depart @ApiIgnore @RequestParam(required = false) Long departId) {
        logger.debug("insert role by userId:" + userId);
        //校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            logger.debug("validate fail");
            return returnObject;
        }
        if(did.equals(departId)){
            Role role = vo.createRole();
            role.setCreatorId(userId);
            role.setDepartId(departId);
            role.setGmtCreate(LocalDateTime.now());
            ReturnObject retObject = roleService.insertRole(role);
            if (retObject.getCode() == ReturnNo.OK) {
                httpServletResponse.setStatus(HttpStatus.CREATED.value());
                return Common.getRetObject(retObject);
            } else {
                return Common.decorateReturnObject(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()));
            }
        }
        else{
            return Common.decorateReturnObject(new ReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE, String.format("部门id不匹配：" + did)));
        }
    }

    /**
     * 删除角色，同时级联删除用户角色表与角色权限表
     *
     * @author 24320182203281 王纬策
     * @param id 角色id
     * @return Object 删除结果
     * createdBy 王纬策 2020/11/04 13:57
     * modifiedBy 王纬策 2020/11/7 19:20
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
    public Object deleteRole(@PathVariable("did") Long did, @PathVariable("id") Long id,
                             @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                             @Depart @ApiIgnore @RequestParam(required = false) Long departId) {
        logger.debug("delete role");
        if(did.equals(departId)){
            ReturnObject returnObject = roleService.deleteRole(departId, id);
            return Common.decorateReturnObject(returnObject);
        }
        else{
            return Common.decorateReturnObject(new ReturnObject<>(ReturnNo.RESOURCE_ID_OUTSCOPE, String.format("部门id不匹配：" + did)));
        }
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
    @Audit
    @PutMapping("/shops/{did}/roles/{id}")
    public Object updateRole(@PathVariable("did") Long did, @PathVariable("id") Long id, @Validated @RequestBody RoleVo vo, BindingResult bindingResult,
                             @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                             @Depart @ApiIgnore @RequestParam(required = false) Long departId) {
        logger.debug("update role by userId:" + userId);
        //校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return returnObject;
        }
        if(did.equals(departId)){
            Role role = vo.createRole();
            role.setId(id);
            role.setDepartId(departId);
            role.setCreatorId(userId);
            role.setGmtModified(LocalDateTime.now());

            ReturnObject retObject = roleService.updateRole(role);
            if (retObject.getData() != null) {
                return Common.getRetObject(retObject);
            } else {
                return Common.decorateReturnObject(new ReturnObject<>(retObject.getCode(), retObject.getErrmsg()));
            }
        }
        else{
            return Common.decorateReturnObject(new ReturnObject<>(ReturnNo.FIELD_NOTVALID, String.format("部门id不匹配：" + did)));
        }
    }
    //endregion
    /* auth008 end*/

    /* auth009 */

    /**
     * auth009: 修改任意用户信息
     * @param id: 用户 id
     * @param vo 修改信息 UserVo 视图
     * @param bindingResult 校验信息
     * @return Object
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:20
     * Modified by 19720182203919 李涵 at 2020/11/8 0:19
     */
    @ApiOperation(value = "修改任意用户信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 732, message = "邮箱已被注册"),
            @ApiResponse(code = 733, message = "电话已被注册"),
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit // 需要认证
    @PutMapping("adminusers/{id}")
    public Object modifyUserInfo(@PathVariable Long id, @Validated @RequestBody UserVo vo, BindingResult bindingResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("modifyUserInfo: id = "+ id +" vo = " + vo);
        }
        // 校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (returnObject != null) {
            logger.info("incorrect data received while modifyUserInfo id = " + id);
            return returnObject;
        }
        ReturnObject returnObj = userService.modifyUserInfo(id, vo);
        return Common.decorateReturnObject(returnObj);
    }

    /**
     * auth009: 删除任意用户
     * @param id: 用户 id
     * @return Object
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:20
     * Modified by 19720182203919 李涵 at 2020/11/8 0:19
     */
    @ApiOperation(value = "删除任意用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit // 需要认证
    @DeleteMapping("adminusers/{id}")
    public Object deleteUser(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("deleteUser: id = "+ id);
        }
        ReturnObject returnObject = userService.deleteUser(id);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * auth009: 禁止用户登录
     * @param id: 用户 id
     * @return Object
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:20
     * Modified by 19720182203919 李涵 at 2020/11/8 0:19
     */
    @ApiOperation(value = "禁止用户登录")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit // 需要认证
    @PutMapping("/shops/{did}/adminusers/{id}/forbid")
    public Object forbidUser(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("forbidUser: id = "+ id);
        }
        ReturnObject returnObject = userService.forbidUser(id);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * auth009: 恢复用户
     * @param id: 用户 id
     * @return Object
     * @author 19720182203919 李涵
     * Created at 2020/11/4 20:20
     * Modified by 19720182203919 李涵 at 2020/11/8 0:19
     */
    @ApiOperation(value = "恢复用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit // 需要认证
    @PutMapping("/shops/{did}/adminusers/{id}/release")
    public Object releaseUser(@PathVariable Long id) {
        if (logger.isDebugEnabled()) {
            logger.debug("releaseUser: id = "+ id);
        }
        ReturnObject returnObject = userService.releaseUser(id);
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
    @PostMapping("adminusers/login")
    public Object login(@Validated @RequestBody LoginVo loginVo, BindingResult bindingResult
            , HttpServletResponse httpServletResponse, HttpServletRequest httpServletRequest){
        /* 处理参数校验错误 */
        Object o = Common.processFieldErrors(bindingResult, httpServletResponse);
        if(o != null){
            return o;
        }

        String ip = IpUtil.getIpAddr(httpServletRequest);
        ReturnObject<String> jwt = userService.login(loginVo.getUserName(), loginVo.getPassword(), ip);

        if(jwt.getData() == null){
            return ResponseUtil.fail(jwt.getCode(), jwt.getErrmsg());
        }else{
            httpServletResponse.setStatus(HttpStatus.CREATED.value());
            return ResponseUtil.ok(jwt.getData());
        }
    }

    /**
     * 用户注销
     * @param userId
     * @return
     * @author 24320182203266
     */
    @ApiOperation(value = "注销")
    @Audit
    @GetMapping("adminusers/logout")
    public Object logout(@LoginUser Long userId){

        logger.debug("logout: userId = "+userId);
        ReturnObject<Boolean> success = userService.Logout(userId);
        if (success.getData() == null)  {
            return ResponseUtil.fail(success.getCode(), success.getErrmsg());
        }else {
            return ResponseUtil.ok();
        }
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
     * @param vo:vo对象
     * @param result 检查结果
     * @return  Object
     * createdBy: LiangJi3229 2020-11-10 18:41
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
    @PostMapping("adminusers")
    public Object register(@Validated @RequestBody NewUserVo vo, BindingResult result){
        if(result.hasErrors()){
            return Common.processFieldErrors(result,httpServletResponse);
        }
        ReturnObject returnObject=newUserService.register(vo);
        if(returnObject.getCode()==ReturnNo.OK){
            return ResponseUtil.ok(returnObject.getData());
        }
        else return ResponseUtil.fail(returnObject.getCode());
    }

    /**
     * 查询所有状态
     * @return Object
     * createdBy: LiangJi3229 2020-11-10 18:41
     */
    @ApiOperation(value="获得管理员用户的所有状态")
    @ApiResponses({
            @ApiResponse(code = 0,message = "成功")
    })
    @GetMapping("adminusers/states")
    public Object getAllStates(){
        User.State[] states=User.State.class.getEnumConstants();
        List<StateVo> stateVos=new ArrayList<StateVo>();
        for(int i=0;i<states.length;i++){
            stateVos.add(new StateVo(states[i]));
        }
        return ResponseUtil.ok(new ReturnObject<List>(stateVos).getData());
    }

    /**
     * auth004: 修改自己的信息
     * @param vo 修改信息 UserVo 视图
     * @param bindingResult 校验信息
     * @return Object
     * @author 24320182203175 陈晓如
     * Created at 2020/11/11 11:22
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
                                        @Validated @RequestBody UserVo vo, BindingResult bindingResult) {
        if (logger.isDebugEnabled()) {
            logger.debug("modifyUserInfo: id = "+ id +" vo = " + vo);
        }
        // 校验前端数据
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (returnObject != null) {
            logger.info("incorrect data received while modifySelfInfo id = " + id);
            return returnObject;
        }
        ReturnObject returnObj = userService.modifyUserInfo(id, vo);
        return Common.decorateReturnObject(returnObj);
    }


    /**
     * auth002: 用户重置密码
     * @param vo 重置密码对象
     * @param httpServletResponse HttpResponse
     * @param httpServletRequest HttpRequest
     * @param bindingResult 校验信息
     * @return Object
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     */
    @ApiOperation(value="用户重置密码")
    @ApiResponses({
            @ApiResponse(code = 745, message = "与系统预留的邮箱不一致"),
            @ApiResponse(code = 746, message = "与系统预留的电话不一致"),
            @ApiResponse(code = 0, message = "成功"),
    })
    @PutMapping("adminusers/password/reset")
    @ResponseBody
    public Object resetPassword(@RequestBody ResetPwdVo vo,BindingResult bindingResult
            , HttpServletResponse httpServletResponse,HttpServletRequest httpServletRequest) {

        if (logger.isDebugEnabled()) {
            logger.debug("resetPassword");
        }
        /* 处理参数校验错误 */
        Object o = Common.processFieldErrors(bindingResult, httpServletResponse);
        if(o != null){
            return o;
        }

        String ip = IpUtil.getIpAddr(httpServletRequest);

        ReturnObject returnObject = userService.resetPassword(vo,ip);
        return Common.decorateReturnObject(returnObject);
    }


    /**
     * auth002: 用户修改密码
     * @param vo 修改密码对象
     * @return Object
     * @author 24320182203311 杨铭
     * Created at 2020/11/11 19:32
     */
    @ApiOperation(value="用户修改密码",produces = "application/json")
    @ApiResponses({
            @ApiResponse(code = 700, message = "用户名不存在或者密码错误"),
            @ApiResponse(code = 741, message = "不能与旧密码相同"),
            @ApiResponse(code = 0, message = "成功"),
    })
    @PutMapping("/adminusers/password")
    @ResponseBody
    public Object modifyPassword(@RequestBody ModifyPwdVo vo) {
        if (logger.isDebugEnabled()) {
            logger.debug("modifyPassword");
        }
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
     * 取消角色权限
     * @return Object
     * createdBy 王琛 24320182203277
     */
    @ApiOperation(value = "取消角色权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="String", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @DeleteMapping("roleprivileges/{id}")
    public Object delRolePriv(@PathVariable Long id){
        logger.debug("delRolePriv: id = "+ id);
        ReturnObject returnObject = roleService.delRolePriv(id);
        return ResponseUtil.fail(returnObject.getCode(), returnObject.getErrmsg());
    }

    /**
     * 增加角色权限
     * @return Object
     * createdBy 王琛 24320182203277
     */
    @ApiOperation(value = "新增角色权限")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="roleid", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="privilegeid", required = true, dataType="String", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit
    @PostMapping("roles/{roleid}/privileges/{privilegeid}")
    public Object addRolePriv(@PathVariable Long roleid, @PathVariable Long privilegeid, @LoginUser @ApiIgnore @RequestParam(required = false, defaultValue = "0") Long userId){
        logger.debug("addRolePriv: id = "+ roleid+" userid: id = "+ userId);
        ReturnObject<VoObject> returnObject = roleService.addRolePriv(roleid, privilegeid, userId);

        if (returnObject.getCode() == ReturnNo.OK) {
            return Common.getRetObject(returnObject);
        } else {
            return Common.decorateReturnObject(returnObject);
        }
    }

    /**
     * auth014: 管理员审核用户
     * @param id: 用户 id
     * @param bindingResult 校验信息
     * @return Object
     * @author 24320182203227 LiZihan
     */
    @ApiOperation(value = "管理员审核用户")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="id", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="did", required = true, dataType="Integer", paramType="path"),
            @ApiImplicitParam(name="approve", required = true, dataType="Boolean", paramType="body")

    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 503, message = "字段不合法"),
            @ApiResponse(code = 705, message = "无权限访问")
    })
    @Audit // 需要认证
    @PutMapping("shops/{did}/adminusers/{id}/approve")
    public Object approveUser(@PathVariable Long id,@PathVariable Long did, BindingResult bindingResult,@RequestBody Boolean approve,@Depart Long shopid) {
        logger.debug("approveUser: did = "+ did+" userid: id = "+ id+" opinion: "+approve);
        ReturnObject returnObject=null;
        if(did==0|| did.equals(shopid))
        {
            returnObject=newUserService.approveUser(approve,id);
        }
        else
        {
            logger.error("approveUser: 无权限查看此部门的用户 did=" + did);
            return new ReturnObject<>(ReturnNo.FIELD_NOTVALID);
        }
        return returnObject;
    }

}

