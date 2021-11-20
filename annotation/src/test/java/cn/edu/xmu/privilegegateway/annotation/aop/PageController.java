package cn.edu.xmu.privilegegateway.annotation.aop;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/", produces = "application/json;charset=UTF-8")
public class PageController {
    @Autowired
    private HttpServletResponse httpServletResponse;

    @GetMapping("/pages/{id}")
    public String test1(@PathVariable("id") Long id, @RequestParam(value = "page",required = false) Integer page, @RequestParam(value = "pageSize",required = false) Integer pageSize) {
        return "{\"data\":" + page + ' ' + pageSize +"}";
    }

}
