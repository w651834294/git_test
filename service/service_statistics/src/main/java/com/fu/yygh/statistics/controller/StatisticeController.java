package com.fu.yygh.statistics.controller;

import com.fu.yygh.common.result.R;
import com.fu.yygh.order.client.OrderFeignClient;
import com.fu.yygh.vo.order.OrderCountQueryVo;
import com.fu.yygh.vo.order.OrderQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/statistics")
public class StatisticeController {
    @Autowired
    private OrderFeignClient orderFeignClient;

    //获取订单数据
    @PostMapping("getCountMap")
    public R getCountMap(OrderCountQueryVo orderCountQueryVo){
        Map<String, Object> map = orderFeignClient.countOrderInfoByQuery(orderCountQueryVo);
        return R.ok().data(map);
    }

}
