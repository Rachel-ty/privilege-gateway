package cn.edu.xmu.privilegegateway.annotation.AOP;

import cn.edu.xmu.privilegegateway.util.JwtHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author GXC
 * @SN 22920192204194
 */
@Aspect
@Component
public class PageAspect {

    //Controller层切点
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RestController)||@within(org.springframework.web.bind.annotation.RestController)")
    public void pageAspect() {
    }
    @Before("pageAspect()")
    public void doBefore(JoinPoint joinPoint) {
    }
    @Around("pageAspect()")
    public Object around(JoinPoint joinPoint){
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        Integer page=1,pageSize=10;
        if(request!=null){
            String pageString= request.getParameter("page");
            String pageSizeString= request.getParameter("pageSize");
            if (pageString!=null&&(!pageString.isEmpty())&&pageString.matches("\\d+")) {
                page=Integer.valueOf(pageString);
            }
            if (pageSizeString!=null&&pageSizeString.matches("\\d+")) {
                pageSize=Integer.valueOf(pageSizeString);
            }
        }
        String[] paramNames = ms.getParameterNames();
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals("page")&& (args[i] == null)) {
                args[i] = page;
            }
            if (paramNames[i].equals("pageSize")&&(args[i] == null)) {
                args[i] = pageSize;
            }
        }
        Object obj = null;
        try {
            obj = ((ProceedingJoinPoint) joinPoint).proceed(args);
        } catch (Throwable e) {

        }
        return obj;
    }
}
