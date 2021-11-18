package cn.edu.xmu.privilegegateway.annotation.controller.annotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;


/**
 * @author zihan zhou
 * @date 2021/11/18
 */

@SpringBootApplication(scanBasePackages = {"cn.edu.xmu.privilegegateway.*"},exclude= {DataSourceAutoConfiguration.class})
@EnableConfigurationProperties
public class AnnotationTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(cn.edu.xmu.privilegegateway.annotation.AnnotationTestApplication.class, args);
    }

}
