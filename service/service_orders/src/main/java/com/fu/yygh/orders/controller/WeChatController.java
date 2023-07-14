package com.fu.yygh.orders.controller;

import com.fu.yygh.common.result.R;
import com.fu.yygh.enums.PaymentTypeEnum;
import com.fu.yygh.orders.service.PaymentService;
import com.fu.yygh.orders.service.WeChatService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/order/weixin")
public class WeChatController {

    @Autowired
    private WeChatService weChatService;

    @Autowired
    private PaymentService paymentService;

    @ApiOperation(value = "根据订单id查询订单的支付状态")
    @GetMapping("/queryPayStatus/{orderId}")
    public R queryPayStatus(@PathVariable("orderId")Long orderId){
        Map<String,String> map=weChatService.queryPayStatus(orderId);
        if(map==null){
            return R.error().message("支付失败");
        }
        //交易状态为SUCCESS,表示支付成功，更新订单状态
        if("SUCCESS".equals(map.get("trade_state"))){
            //更新订单状态 更新支付记录状态
            String out_trade_no = map.get("out_trade_no");
            paymentService.paySuccess(out_trade_no, PaymentTypeEnum.WEIXIN.getStatus(),map);
            return R.ok().message("支付成功");
        }

        return R.ok().message("支付中");
    }



    //根据订单id生成订单
    @GetMapping("/createNative/{orderId}")
    public R createNative(@PathVariable("orderId")Long orderId){
        Map<String,Object> map=weChatService.createNative(orderId);
        return R.ok().data(map);
    }
}
