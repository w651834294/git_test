package com.fu.yygh.orders.listener;

import com.fu.yygh.orders.service.OrderInfoService;
import com.fu.yygh.rabbit.MqConst;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderReceiver {

    @Autowired
    private OrderInfoService orderInfoService;;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_TASK_8,durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_TASK),
            key = {MqConst.ROUTING_TASK_8}
    ))
    public void patientTips(Message message, Channel channel)throws IOException{
        orderInfoService.patientTips();
    }
}
