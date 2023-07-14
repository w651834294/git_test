package com.fu.yygh.order.client;

import com.fu.yygh.vo.order.OrderCountQueryVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(value = "service-orders")
@Repository
public interface OrderFeignClient {
    //获取订单统计数据
    @PostMapping("/api/order/orderInfo/countOrderInfoByQuery")
    Map<String,Object> countOrderInfoByQuery(@RequestBody OrderCountQueryVo orderCountQueryVo);
}
