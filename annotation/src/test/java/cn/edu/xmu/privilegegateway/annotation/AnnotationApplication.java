package cn.edu.xmu.privilegegateway.annotation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AnnotationApplication {

    public static void main(String[] args) {

        SpringApplication.run(AnnotationApplication.class, args);

    }

}
