package com.fu.yygh.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@ComponentScan(basePackages = "com.fu")//扫描当前包及其依赖包
@EnableDiscoveryClient//把当前微服务注册到nacos注册中心
@EnableFeignClients(basePackages = "com.fu")//远程调用
@MapperScan("com.fu.yygh.user.mapper")//扫描mapper层
public class ServiceUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceUserApplication.class,args);
    }
}
