package com.fu.yygh.orders.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fu.yygh.enums.OrderStatusEnum;
import com.fu.yygh.enums.PaymentStatusEnum;
import com.fu.yygh.model.order.OrderInfo;
import com.fu.yygh.model.order.PaymentInfo;
import com.fu.yygh.orders.mapper.OrderInfoMapper;
import com.fu.yygh.orders.mapper.PaymentMapper;
import com.fu.yygh.orders.service.OrderInfoService;
import com.fu.yygh.orders.service.PaymentService;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {


    @Autowired
    private OrderInfoMapper orderInfoMapper;


    //保存交易记录
    @Override
    public void savePaymentInfo(OrderInfo order, Integer paymentType) {
        //如果订单已支付，就不用支付了
        QueryWrapper<PaymentInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("order_id",order.getId());
        Integer count = baseMapper.selectCount(queryWrapper);
        if(count>0){
            return;
        }
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+"|"+order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(order.getAmount());
        baseMapper.insert(paymentInfo);
    }

    @Override
    public void paySuccess(String out_trade_no, Integer paymentType, Map<String, String> map) {
        //1.更新订单状态
        QueryWrapper<OrderInfo> queryWrapperOrder=new QueryWrapper<>();
        queryWrapperOrder.eq("out_trade_no",out_trade_no);
        OrderInfo orderInfo = orderInfoMapper.selectOne(queryWrapperOrder);
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderInfoMapper.updateById(orderInfo);
        //2.更新支付记录状态
        QueryWrapper<PaymentInfo> queryWrapperPayment=new QueryWrapper<>();
        queryWrapperPayment.eq("out_trade_no",out_trade_no);
        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapperPayment);
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setTradeNo(map.get("transaction_id"));
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(map.toString());
        baseMapper.updateById(paymentInfo);
    }
}
