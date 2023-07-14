package com.fu.yygh.orders.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fu.yygh.enums.RefundStatusEnum;
import com.fu.yygh.model.order.PaymentInfo;
import com.fu.yygh.model.order.RefundInfo;
import com.fu.yygh.orders.mapper.RefundInfoMapper;
import com.fu.yygh.orders.service.RefundInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 * 退款信息表 服务实现类
 * </p>
 *
 * @author fu
 * @since 2023-07-12
 */
@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper, RefundInfo> implements RefundInfoService {

    //保存退款记录
    @Override
    public RefundInfo saveRefund(PaymentInfo paymentInfo) {
        QueryWrapper<RefundInfo> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("order_id",paymentInfo.getOrderId());
        RefundInfo refundInfo1 = baseMapper.selectOne(queryWrapper);
        if(refundInfo1 !=null){
            return refundInfo1;
        }

        refundInfo1 = new RefundInfo();
        refundInfo1.setOrderId(paymentInfo.getOrderId());
        refundInfo1.setPaymentType(paymentInfo.getPaymentType());
        refundInfo1.setOutTradeNo(paymentInfo.getOutTradeNo());
        refundInfo1.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());
        refundInfo1.setSubject(paymentInfo.getSubject());
        refundInfo1.setTotalAmount(paymentInfo.getTotalAmount());
        //refundInfo1.setTradeNo(paymentInfo.getTradeNo());
        baseMapper.insert(refundInfo1);
        return refundInfo1;
    }
}
