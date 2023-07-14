package com.fu.yygh.task;

import com.fu.yygh.rabbit.MqConst;
import com.fu.yygh.rabbit.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

@Component
public class ScheduledTask {

    @Autowired
    private RabbitService rabbitService;

    @Scheduled(cron = "0/30 * * * * ?")
    public void printTime(){
        System.out.println("当前时间为："+new SimpleDateFormat("yyyy-MM-dd HH:mm:sss").format(new Date()));
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_TASK,MqConst.ROUTING_TASK_8,"");
    }
}
