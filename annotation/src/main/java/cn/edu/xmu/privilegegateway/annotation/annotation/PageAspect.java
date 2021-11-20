package cn.edu.xmu.privilegegateway.annotation.annotation;

import cn.edu.xmu.privilegegateway.util.JwtHelper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Aspect
@Component
public class PageAspect {

    //Controller层切点
    @Pointcut("@annotation(cn.edu.xmu.privilegegateway.annotation.annotation.PageDefault)||@within(cn.edu.xmu.privilegegateway.annotation.annotation.PageDefault)")
    public void pageAspect() {
    }
    @Before("pageAspect()")
    public void doBefore(JoinPoint joinPoint) {
    }
    @Around("pageAspect()")
    public Object around(JoinPoint joinPoint){
        MethodSignature ms = (MethodSignature) joinPoint.getSignature();
        Method method = ms.getMethod();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        String pageString= request.getParameter("page");
        String pageSizeString= request.getParameter("pageSize");
        Integer page=1,pageSize=10;
        if (pageString!=null&&(!pageString.isEmpty())&&pageString.matches("\\d+")) {
            page=Integer.valueOf(pageString);
        }
        if (pageSizeString!=null&&pageSizeString.matches("\\d+")) {
            pageSize=Integer.valueOf(pageSizeString);
        }
        Object[] args = joinPoint.getArgs();
        Annotation[][] annotations = method.getParameterAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Object param = args[i];
            Annotation[] paramAnn = annotations[i];
            if (paramAnn.length == 0){
                continue;
            }
            for (Annotation annotation : paramAnn) {
                if (annotation.annotationType().equals(Page.class)) {
                    //校验该参数，验证一次退出该注解
                    args[i] = page;
                }
                if (annotation.annotationType().equals(PageSize.class)) {
                    //校验该参数，验证一次退出该注解
                    args[i] = pageSize;
                }
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
