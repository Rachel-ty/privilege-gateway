package cn.edu.xmu.privilegegateway.privilegeservice.controller;


import cn.edu.xmu.privilegegateway.annotation.aop.Audit;
import cn.edu.xmu.privilegegateway.annotation.aop.Depart;
import cn.edu.xmu.privilegegateway.annotation.aop.LoginName;
import cn.edu.xmu.privilegegateway.annotation.aop.LoginUser;
import cn.edu.xmu.privilegegateway.annotation.util.Common;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnObject;
import cn.edu.xmu.privilegegateway.privilegeservice.model.bo.Group;
import cn.edu.xmu.privilegegateway.privilegeservice.model.vo.GroupVo;
import cn.edu.xmu.privilegegateway.privilegeservice.service.GroupService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@SpringBootApplication(scanBasePackages = {"cn.edu.xmu.privilegegateway"})
@Api(value = "权限服务", tags = "privilege")
@RestController /*Restful的Controller对象*/
@RequestMapping(value = "/", produces = "application/json;charset=UTF-8")
public class GroupController {
    private  static  final Logger logger = LoggerFactory.getLogger(GroupController.class);

    @Autowired
    private GroupService groupService;


    @Autowired
    private HttpServletResponse httpServletResponse;


    /**
     * 获得部门的所有用户组
     * @param did
     * @param page
     * @param pageSize
     * @return
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "获得部门的所有用户组")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/groups")
    public Object getallgroup(@PathVariable Long did, @RequestParam(required = false) Integer page,
                              @RequestParam(required = false) Integer pageSize,
                              @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                              @RequestParam(required = false) @LoginName @ApiIgnore String userName){
        if(did==null || did<0)
            return new ReturnObject(ReturnNo.FIELD_NOTVALID,"部门名不合法");
        ReturnObject returnObject =  groupService.getAllgroups(did,page,pageSize);
        return Common.decorateReturnObject(returnObject);

    }

    /**
     * 获得组里的所有用户
     * @param did
     * @param page
     * @param pageSize
     * @return
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "获得组里的所有用户")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", value="组id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/groups/{id}/users")
    public Object getgroupuser(@PathVariable Long did,@PathVariable Long id, @RequestParam(required = false) Integer page,
                               @RequestParam(required = false) Integer pageSize,
                               @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                               @RequestParam(required = false) @LoginName @ApiIgnore String userName){if(did==null || did<0)
        return new ReturnObject(ReturnNo.FIELD_NOTVALID,"部门名不合法");
        ReturnObject returnObject =  groupService.getgroupsuser(did,id,page,pageSize);
        return Common.decorateReturnObject(returnObject);

    }
    /**
     * 获得用户的组
     * @param did
     * @param page
     * @param pageSize
     * @return
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "获得用户的组")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", value="组id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/users/{id}/groups")
    public Object getusersgroup(@PathVariable Long did,@PathVariable Long id, @RequestParam(required = false) Integer page,
                                @RequestParam(required = false) Integer pageSize,
                                @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                @RequestParam(required = false) @LoginName @ApiIgnore String userName){
        ReturnObject returnObject =  groupService.getusersgroup(did,id,page,pageSize);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 新获得用户的组
     * @param page
     * @param pageSize
     * @param userId
     * @param userName
     * @return
     */

    @ApiOperation(value = "获得用户的组")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @GetMapping("/self/groups")
    public Object getusersgroup(@RequestParam(required = false) Integer page,
                                @RequestParam(required = false) Integer pageSize,
                                @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                @RequestParam(required = false) @LoginName @ApiIgnore String userName){
        ReturnObject returnObject =  groupService.getusersgroup(0L,userId,page,pageSize);
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 修改用户组的信息
     * @param did
     * @param id
     * @param groupVo
     * @param bindingResult
     * @param httpServletResponse
     * @return
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "修改用户组的信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name="authorization", value="Token", required = true, dataType="String", paramType="header"),
            @ApiImplicitParam(name="did", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="id", required = true, dataType="String", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @PutMapping("/departs/{did}/groups/{id}")
    public Object changeGroup(@PathVariable Long did, @PathVariable Long id, @Validated @RequestBody GroupVo groupVo, BindingResult bindingResult,
                              @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                              @RequestParam(required = false) @LoginName @ApiIgnore String userName,
                              HttpServletResponse httpServletResponse){
        /* 处理参数校验错误 */
        Object o = Common.processFieldErrors(bindingResult, httpServletResponse);
        if(o != null){
            return o;
        }
        Group group = (Group) Common.cloneVo(groupVo,Group.class);
        ReturnObject returnObject = groupService.changeGroup(did,id, group,userId,userName);
        return Common.decorateReturnObject(returnObject);
    }


    /**
     * 新增一个用户组
     *
     * @param bindingResult 校验错误
     * @param did 部门id
     * @return Object 用户组返回视图
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "新增用户组", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "body", dataType = "GroupVo", name = "vo", value = "组名", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 736, message = "组名已存在"),
    })
    @Audit(departName = "departs")
    @PostMapping("/departs/{did}/groups")
    public Object insertgroup(@LoginUser Long userId,
                              @LoginName String userName,
                              @Depart Long depart_id, @PathVariable(name="did") Long did,
                              @RequestBody @Valid GroupVo vo, BindingResult bindingResult
    ) {
        Object returnObject = Common.processFieldErrors(bindingResult, httpServletResponse);
        if (null != returnObject) {
            return returnObject;
        }
        if(!did.equals(depart_id))
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        ReturnObject retObject = groupService.insertGroup(vo,did,userId,userName);
        return Common.decorateReturnObject(retObject);

    }

    /**
     * 增加用户组pid的子用户组sid
     *
     * @param departId 部门id
     * @return Object 用户组返回视图
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "增加用户组pid的子用户组sid", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="did", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="pid", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="sid", required = true, dataType="String", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 736, message = "组名已存在"),
    })
    @Audit(departName = "departs")
    @PostMapping("/departs/{did}/groups/{pid}/subgroups/{sid}")
    public Object insertgroupRelation(@PathVariable Long did,@PathVariable Long pid,@PathVariable Long sid,
                                      @LoginUser Long userId,
                                      @LoginName String userName,
                                      @Depart Long departId) {
        ReturnObject retObject = groupService.insertGroupRelation(did,pid,sid,userId,userName);
        return Common.decorateReturnObject(retObject);
    }

    /**
     * 取消用户组pid和用户组sid的父子关系
     * @param did
     * @param pid
     * @param sid
     * @param userId
     * @param userName
     * @param departId
     * @return
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "取消用户组pid和用户组sid的父子关系", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="did", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="pid", required = true, dataType="String", paramType="path"),
            @ApiImplicitParam(name="sid", required = true, dataType="String", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 736, message = "组名已存在"),
    })
    @Audit(departName = "departs")
    @DeleteMapping("/departs/{did}/groups/{pid}/subgroups/{sid}")
    public Object deletegroupRelation(@PathVariable Long did,@PathVariable Long pid,@PathVariable Long sid,
                                      @LoginUser Long userId,
                                      @LoginName String userName,
                                      @Depart Long departId) {
        if(pid.equals(sid))
            return new ReturnObject(ReturnNo.FIELD_NOTVALID,"父子用户组id不能相同");
        ReturnObject retObject = groupService.deleteGroupRelation(did,pid,sid,userId,userName);
        return Common.decorateReturnObject(retObject);
    }


    /**
     * 删除用户组需删除用户组的角色，删除用户组的父子关系
     *
     * @param id 用户组id
     * @return Object 删除结果
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "删除用户组", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "did", value = "部门id", required = true),
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "id", value = "用户组id", required = true)
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
    })
    @Audit(departName = "departs")
    @DeleteMapping("/departs/{did}/groups/{id}")
    public Object deleteGroup(@PathVariable("did") Long did, @PathVariable("id") Long id,
                              @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                              @RequestParam(required = false) @LoginName @ApiIgnore String userName,
                              @Depart @ApiIgnore @RequestParam(required = false) Long departId) {
        if(!did.equals(departId))
            return Common.decorateReturnObject(new ReturnObject(ReturnNo.RESOURCE_ID_OUTSCOPE));
        ReturnObject returnObject = groupService.deleteGroup(departId, id);
        return Common.decorateReturnObject(returnObject);
    }


    /**
     * 查询用户组所有状态
     * @return Object
     * createdBy:  Weining Shi
     */
    @ApiOperation(value="获得用户组的所有状态")
    @ApiResponses({
            @ApiResponse(code = 0,message = "成功")
    })
    @GetMapping("/groups/states")
    public Object getAllgroupStates(){
        ReturnObject returnObject=groupService.getAllStates();
        return Common.decorateReturnObject(returnObject);
    }

    /**
     * 获得用户组id的子用户组
     * @param did
     * @param page
     * @param pageSize
     * @return
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "获得用户组id的子用户组")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", value="用户组id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/groups/{id}/subgroups")
    public Object getsubGroup(@PathVariable Long did,@PathVariable Long id, @RequestParam(required = false) Integer page,
                              @RequestParam(required = false) Integer pageSize,
                              @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                              @RequestParam(required = false) @LoginName @ApiIgnore String userName){
        ReturnObject returnObject =  groupService.getsubGroup(did,id,page,pageSize);
        return Common.decorateReturnObject(returnObject);

    }

    /**
     * 获得用户组id的父用户组
     * @param did
     * @param page
     * @param pageSize
     * @return
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "获得用户组id的父用户组")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", value="用户组id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功")
    })
    @Audit(departName = "departs")
    @GetMapping("/departs/{did}/groups/{id}/parents")
    public Object getparGroup(@PathVariable Long did,@PathVariable Long id, @RequestParam(required = false) Integer page,
                              @RequestParam(required = false) Integer pageSize,
                              @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                              @RequestParam(required = false) @LoginName @ApiIgnore String userName){
        ReturnObject returnObject =  groupService.getparGroup(did,id,page,pageSize);
        return Common.decorateReturnObject(returnObject);

    }

    /**
     * 将用户加入组
     *
     * @return Object 用户组返回视图
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "将用户加入组", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", value="用户组id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="uid", value="用户id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 505, message = "操作的资源id不是自己的对象"),
    })
    @Audit(departName = "departs")
    @PostMapping("/departs/{did}/groups/{id}/users/{uid}")
    public Object insertusergroup(@PathVariable Long did,@PathVariable Long id,@PathVariable Long uid,
                                  @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                  @RequestParam(required = false) @LoginName @ApiIgnore String userName) {
        ReturnObject retObject = groupService.insertUserGroup(uid,id,did,userId,userName);
        return Common.decorateReturnObject(retObject);
    }

    /**
     * 将用户加入剔除出组
     * @param departId 部门id
     * @return Object 用户组返回视图
     * createdBy:  Weining Shi
     */
    @ApiOperation(value = "将用户剔除出组", produces = "application/json")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "header", dataType = "String", name = "authorization", value = "Token", required = true),
            @ApiImplicitParam(name="did", value="部门id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="id", value="用户组id", required = true, dataType="int", paramType="path"),
            @ApiImplicitParam(name="uid", value="用户id", required = true, dataType="int", paramType="path")
    })
    @ApiResponses({
            @ApiResponse(code = 0, message = "成功"),
            @ApiResponse(code = 505, message = "操作的资源id不是自己的对象"),
    })
    @Audit(departName = "departs")
    @DeleteMapping("/departs/{did}/groups/{id}/users/{uid}")
    public Object deleteusergroup(@PathVariable Long did,@PathVariable Long id,@PathVariable Long uid,
                                  @LoginUser @ApiIgnore @RequestParam(required = false) Long userId,
                                  @RequestParam(required = false) @LoginName @ApiIgnore String userName,
                                  @Depart @ApiIgnore @RequestParam(required = false) Long departId) {
        ReturnObject retObject = groupService.deleteUserGroup(uid,id,did,userId,userName);
        return Common.decorateReturnObject(retObject);
    }

    /**
     * 禁用用户组
     * @param did
     * @param id
     * @param loginUser
     * @param loginUsername
     * @return
     * createdBy:  Weining Shi
     */

    @ApiOperation(value = "禁用用户组",produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功"),
            @ApiResponse(code = 505, message = "操作的资源id不是自己的对象")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "adminToken", required = true, dataType = "String", paramType = "header")
    })
    @PutMapping(value = "/departs/{did}/group/{id}/forbid")
    @Audit(departName = "departs")
    public Object forbidGroup(@PathVariable("did") Long did,@PathVariable("id") Long id,Long loginUser,String loginUsername){

        loginUser=1L;
        loginUsername="1";
        ReturnObject ret= groupService.forbidGroup(did,id, loginUser,loginUsername);
        return Common.decorateReturnObject(ret);
    }

    /**
     * 解禁用户组
     * @param did
     * @param id
     * @param loginUser
     * @param loginUsername
     * @return
     * createdBy:  Weining Shi
     */

    @ApiOperation(value = "解禁用户组",produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "成功"),
            @ApiResponse(code = 505, message = "操作的资源id不是自己的对象")
    })
    @ApiImplicitParams({
            @ApiImplicitParam(name = "authorization", value = "adminToken", required = true, dataType = "String", paramType = "header")
    })
    @PutMapping(value = "/departs/{did}/groups/{id}/release")
    @Audit(departName = "departs")
    public Object releaseGroup(@PathVariable("did") Long did,@PathVariable("id") Long id,@LoginUser Long loginUser,@LoginName String loginUsername){
        ReturnObject ret= groupService.releaseGroup(did,id, loginUser,loginUsername);
        return Common.decorateReturnObject(ret);
    }




}
