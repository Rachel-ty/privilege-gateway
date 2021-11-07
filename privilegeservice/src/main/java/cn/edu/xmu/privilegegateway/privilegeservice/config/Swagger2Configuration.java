package cn.edu.xmu.privilegegateway.privilegeservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Response;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

import cn.edu.xmu.privilegegateway.util.ReturnNo;

@Configuration
@EnableSwagger2
/**
 * @author Ming Qiu
 * @date 2020/3/13 15:46
 */
public class  Swagger2Configuration {

    public static final String SWAGGER_SCAN_BASE_PACKAGE = "cn.edu.xmu.privilegegateway.privilegeservice.controller";

    public static final String VERSION = "1.0.0";

    @Bean
    public Docket createRestApi() {
        List<Response> getResponseList = new ArrayList<>();
        getResponseList.add(new ResponseBuilder().code("500").description("{errno"+ ReturnNo.INTERNAL_SERVER_ERR+"}}").build());
        return new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(true)
                .globalResponses(HttpMethod.GET, getResponseList)
                .globalResponses(HttpMethod.POST, getResponseList)
                .globalResponses(HttpMethod.DELETE, getResponseList)
                .globalResponses(HttpMethod.PUT, getResponseList)
                .apiInfo(apiInfo())
                .select()
                //为当前包路径
                .apis(RequestHandlerSelectors.basePackage(SWAGGER_SCAN_BASE_PACKAGE))
                .paths(PathSelectors.any())
                .build();
    }

    //构建 api文档的详细信息函数,注意这里的注解引用的是哪个
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //页面标题
                .title("RBAC0级权限管理网关系统")
                //描述
                .description("Restful API接口")
                //创建人
                .contact(new Contact("Ming Qiu", "http://software.xmu.edu.cn", "mingqiu@xmu.edu.cn"))
                //版本号
                .version(VERSION)
                //license
                .termsOfServiceUrl("http://software.xmu.edu.cn")
                .build();
    }
}
