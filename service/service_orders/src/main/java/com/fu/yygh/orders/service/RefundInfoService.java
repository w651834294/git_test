package com.fu.yygh.orders.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.fu.yygh.model.order.PaymentInfo;
import com.fu.yygh.model.order.RefundInfo;

/**
 * <p>
 * 退款信息表 服务类
 * </p>
 *
 * @author fu
 * @since 2023-07-12
 */
public interface RefundInfoService extends IService<RefundInfo> {

    RefundInfo saveRefund(PaymentInfo paymentInfo);
}
