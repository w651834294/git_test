package com.fu.yygh.orders;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.fu")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.fu")
@MapperScan("com.fu.yygh.orders.mapper")
public class ServiceOrdersApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrdersApplication.class,args);
    }
}
