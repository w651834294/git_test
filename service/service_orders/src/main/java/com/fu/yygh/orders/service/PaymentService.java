package com.fu.yygh.orders.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fu.yygh.model.order.OrderInfo;
import com.fu.yygh.model.order.PaymentInfo;

import java.util.Map;


public interface PaymentService extends IService<PaymentInfo> {
    //保存交易记录
    void savePaymentInfo(OrderInfo order,Integer paymentType);

    void paySuccess(String out_trade_no, Integer status, Map<String, String> map);
}
