package cn.edu.xmu.privilegegateway.annotation.aop;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 参数效验AOP
 */
@Component
@Aspect
public class PageAOP {

    /**
     * 定义有一个切入点，范围为web包下的类
     */
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RestController)||@within(org.springframework.web.bind.annotation.RestController)")


    public void pageAspect() {
    }

    @Before("pageAspect()")
    public void doBefore(JoinPoint joinPoint) {
    }

    @Around("pageAspect()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature signature = ((MethodSignature) pjp.getSignature());
        //得到拦截的方法
        Method method = signature.getMethod();

        //获取方法参数名
        String[] paramNames = signature.getParameterNames();
        //获取参数值
        Object[] paranValues = pjp.getArgs();
        //获取方法参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals("page") && (paranValues[i] == null)) {
                paranValues[i] = Integer.valueOf(1);
            }
            if (paramNames[i].equals("pageSize") && (paranValues[i] == null)) {
                paranValues[i] = Integer.valueOf(10);
            }
        }

        return pjp.proceed(paranValues);
    }

    @AfterReturning(value = "pageAspect()", returning = "result")
    public void doAfterReturning(JoinPoint joinPoint, Object result) {

    }


}
