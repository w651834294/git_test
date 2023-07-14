package com.fu.yygh.cmn;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication(exclude = MongoAutoConfiguration.class)
@ComponentScan(basePackages = "com.fu")
@MapperScan(basePackages = "com.fu.yygh.cmn.mapper")
@EnableDiscoveryClient
public class ServiceCmnApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceCmnApplication.class,args);
    }
}
