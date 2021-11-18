package cn.edu.xmu.privilegegateway.annotation.controller.annotation;

import cn.edu.xmu.privilegegateway.annotation.annotation.Audit;
import cn.edu.xmu.privilegegateway.annotation.annotation.Depart;
import cn.edu.xmu.privilegegateway.annotation.annotation.LoginName;
import cn.edu.xmu.privilegegateway.annotation.annotation.LoginUser;
import cn.edu.xmu.privilegegateway.annotation.util.ResponseUtil;
import cn.edu.xmu.privilegegateway.annotation.util.ReturnNo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author zihan zhou
 * @date 2021/11/18
 */
@RestController
@RequestMapping(value = "/privilege",produces = "application/json;charset=UTF-8")
public class TryController {
    @Autowired
    private HttpServletResponse httpServletResponse;
    //没有common 我先引进来了
    @GetMapping("/shops/{id}")
    @Audit(departName = "shops")
    public String test1(@PathVariable("id") Long id,@LoginUser Long userId, @Depart @RequestParam(required = false) Long departId, @LoginName String userName){
        System.out.println(userId.toString()+' '+departId.toString()+' '+userName);
        return "{ \"data\":"+userId.toString()+' '+departId.toString()+' '+userName+"}";


    }
    @GetMapping("/try/{id}")
    @Audit(departName = "try")
    public Object test2(@PathVariable("id") Long id, @LoginUser Long userId, @Depart @RequestParam(required = false) Long departId, @LoginName String userName){

        return "{ \"data\":"+userId.toString()+' '+departId.toString()+' '+userName+"}";


    }
    @GetMapping("/try1/{id}")
    @Audit(departName = "try")
    public Object test3(@PathVariable("id") Long id, @LoginUser Long userId, @Depart @RequestParam(required = false) Long departId, @LoginName String userName){

        System.out.println(userId.toString()+' '+departId.toString()+' '+userName);
        return "{ \"data\":"+userId.toString()+' '+departId.toString()+' '+userName+"}";


    }
}
