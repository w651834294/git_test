package com.fu.yygh.hosp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.fu"})
@MapperScan(basePackages = "com.fu.yygh.hosp.mapper")
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.fu")//必须指定，依赖的jar包也会扫描
public class ServiceHospApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceHospApplication.class,args);
    }
}
