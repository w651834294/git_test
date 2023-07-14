package com.fu.yygh.hosp.listener;

import com.fu.yygh.hosp.service.ScheduleService;
import com.fu.yygh.model.hosp.Schedule;
import com.fu.yygh.rabbit.MqConst;
import com.fu.yygh.rabbit.RabbitService;
import com.fu.yygh.vo.msm.MsmVo;
import com.fu.yygh.vo.order.OrderMqVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.stereotype.Component;

@Component
public class HospitalReceiver {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private RabbitService rabbitService;

    @RabbitListener(bindings = {
            @QueueBinding(value = @Queue(name = MqConst.QUEUE_ORDER,durable = "true"),
            exchange = @Exchange(name = MqConst.EXCHANGE_DIRECT_ORDER),
            key = MqConst.ROUTING_ORDER)

    })
    public void consumer(OrderMqVo orderMqVo, Message message, Channel channel){
        scheduleService.update(orderMqVo);

        MsmVo msmVo = orderMqVo.getMsmVo();
        if(msmVo!=null){
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM,MqConst.ROUTING_MSM_ITEM, msmVo);
        }
    }
}
