package cn.edu.xmu.privilegegateway.annotation.controller.annotation;

import cn.edu.xmu.privilegegateway.annotation.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zihan zhou
 * @date 2021/11/18
 */
@RestController
@RequestMapping(value = "/privilege", produces = "application/json;charset=UTF-8")
public class TryController {
    @Autowired
    private HttpServletResponse httpServletResponse;

    @GetMapping("/shops")
    public String test1(@RequestParam(value = "XX") Integer page,
                        Integer pageSize) {
        return  "{ \"data\":" + page.toString()+pageSize.toString() + "}";
    }
    //没有common 我先引进来了
    @GetMapping("/shops/{id}")
    @Audit(departName = "shops")
    public String test1(@PathVariable("id") Long id, @LoginUser Long userId, @Depart @RequestParam(required = false) Long departId, @LoginName String userName, @UserLevel Integer userLevel) {

        return "{ \"data\":" + userId.toString() + ' ' + departId.toString() + ' ' + userName +' ' + userLevel + "}";


    }

    @GetMapping("/try/{id}")
    @Audit(departName = "try")
    public Object test2(@PathVariable("id") Long id, @LoginUser Long userId, @Depart @RequestParam(required = false) Long departId, @LoginName String userName,@UserLevel Integer userLevel) {

        return "{ \"data\":" + userId.toString() + ' ' + departId.toString() + ' ' + userName + ' ' + userLevel +"}";


    }

    @GetMapping("/try1/{id}")
    @Audit(departName = "try")
    public Object test3(@PathVariable("id") Long id, @LoginUser Long userId, @Depart @RequestParam(required = false) Long departId, @LoginName String userName,@UserLevel Integer userLevel) {


        return "{ \"data\":" + userId.toString() + ' ' + departId.toString() + ' ' + userName + ' ' + userLevel + "}";


    }
    @GetMapping("/try2/{id}")
    @Audit()
    public Object test4(@PathVariable("id") Long id, @LoginUser Long userId, @Depart @RequestParam(required = false) Long departId, @LoginName String userName,@UserLevel Integer userLevel) {

        if(departId==null)
        return "{ \"data\": departId==null }";
        else return "{ \"data\": departId!=null }";


    }
}
