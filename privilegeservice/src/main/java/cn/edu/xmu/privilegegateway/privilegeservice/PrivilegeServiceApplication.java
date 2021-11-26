package cn.edu.xmu.privilegegateway.privilegeservice;

import cn.edu.xmu.privilegegateway.privilegeservice.dao.PrivilegeDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.RoleDao;
import cn.edu.xmu.privilegegateway.privilegeservice.dao.UserDao;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Ming Qiu
 **/
@SpringBootApplication(scanBasePackages = {"cn.edu.xmu.privilegegateway","cn.edu.xmu.privilegegateway.privilegeservice"})
@MapperScan(basePackages = "cn.edu.xmu.privilegegateway.privilegeservice.mapper")
@EnableDiscoveryClient
public class PrivilegeServiceApplication implements ApplicationRunner {

    private  static  final Logger logger = LoggerFactory.getLogger(PrivilegeServiceApplication.class);
    /**
     * 是否初始化，生成signature和加密
     */
    @Value("${privilegeservice.initialization}")
    private Boolean initialization;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PrivilegeDao privilegeDao;

    public static void main(String[] args) {
        SpringApplication.run(PrivilegeServiceApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (initialization){
            logger.debug("Initialize......");
            userDao.initialize();
            roleDao.initialize();
            privilegeDao.initialize();
        }
    }
}
