package com.fu.yygh.statistics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, MongoAutoConfiguration.class})
@ComponentScan(basePackages = "com.fu")
@EnableFeignClients(basePackages = "com.fu")
@EnableDiscoveryClient
public class ServiceStatisticsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceStatisticsApplication.class,args);
    }
}
