package com.fu.yygh.orders.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fu.yygh.common.exception.YYGHException;
import com.fu.yygh.enums.PaymentTypeEnum;
import com.fu.yygh.enums.RefundStatusEnum;
import com.fu.yygh.model.order.OrderInfo;
import com.fu.yygh.model.order.PaymentInfo;
import com.fu.yygh.model.order.RefundInfo;
import com.fu.yygh.orders.service.OrderInfoService;
import com.fu.yygh.orders.service.PaymentService;
import com.fu.yygh.orders.service.RefundInfoService;
import com.fu.yygh.orders.service.WeChatService;
import com.fu.yygh.orders.utils.ConstantPropertiesUtils;
import com.fu.yygh.orders.utils.HttpClient;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class WeChatServiceImpl implements WeChatService {

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private PaymentService paymentService;
    //根据订单id获取生成微信支付二维码所需要的数据

    @Autowired
    private RefundInfoService refundInfoService;


    @Override
    public Map<String, Object> createNative(Long orderId) {

        try {
            //1.根据订单id获取订单信息
            OrderInfo orderInfo = orderInfoService.getOrders(orderId);
            //2.添加支付交易记录
            paymentService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
            //3.准备参数，xml格式 调用微信服务为接口进行支付

            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            Date reserveDate = orderInfo.getReserveDate();
            String reserveDateString = new DateTime(reserveDate).toString("yyyy/MM/dd");
            String body = reserveDateString + "就诊"+ orderInfo.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1");//为了测试
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");

            //2、HTTPClient来根据URL访问第三方接口并且传递参数
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            //client设置参数
            client.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            client.setHttps(true);
            client.post();

            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);

            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));
            //4.拿到微信返回结果，
            return map;
        }catch (Exception e){
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, String> queryPayStatus(Long orderId) {
        try {
            //根据订单id查询订单信息
            OrderInfo orderInfo = orderInfoService.getOrders(orderId);
            Map<String,String> map=new HashMap<>();
            map.put("appid",ConstantPropertiesUtils.APPID);
            map.put("mch_id",ConstantPropertiesUtils.PARTNER);
            map.put("out_trade_no",orderInfo.getOutTradeNo());
            map.put("nonce_str",WXPayUtil.generateNonceStr());



            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(map,ConstantPropertiesUtils.PARTNERKEY));
            httpClient.setHttps(true);
            httpClient.post();
            String content = httpClient.getContent();
            return WXPayUtil.xmlToMap(content);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


    }

    @Override
    public boolean refund(Long orderId) {

        try {
            //2.2.1 往退款表中添加一条退款数据，同时请求微信服务器进行退款
            QueryWrapper<PaymentInfo> queryWrapper=new QueryWrapper<>();
            queryWrapper.eq("order_id",orderId);
            PaymentInfo paymentInfo = paymentService.getOne(queryWrapper);
            if(paymentInfo==null){
                throw new YYGHException(2001,"没有该订单的支付记录");
            }
            RefundInfo refundInfo = refundInfoService.saveRefund(paymentInfo);
            if(refundInfo.getRefundStatus().intValue()==RefundStatusEnum.REFUND.getStatus().intValue()){
                return true;
            }

            Map<String,String> map=new HashMap<>();
            map.put("appid",ConstantPropertiesUtils.APPID);
            map.put("mch_id",ConstantPropertiesUtils.PARTNER);
            map.put("nonce_str",WXPayUtil.generateNonceStr());
            map.put("transaction_id",paymentInfo.getTradeNo());
            map.put("out_trade_no",paymentInfo.getOutTradeNo());
            map.put("out_refund_no","tk"+paymentInfo.getOutTradeNo());
            map.put("total_fee","1");//支付的金额
            map.put("refund_fee","1");//退款的金额

            String paramXml = WXPayUtil.generateSignedXml(map, ConstantPropertiesUtils.PARTNERKEY);
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            httpClient.setXmlParam(paramXml);
            httpClient.setHttps(true);
            httpClient.setCert(true);//需要证书支持
            httpClient.setCertPassword(ConstantPropertiesUtils.PARTNER);
            httpClient.post();

            String content = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);
            if(resultMap!=null&& WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))){
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setCallbackTime(new Date());
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
}
