package com.fu.yygh.orders.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fu.yygh.common.result.R;
import com.fu.yygh.enums.OrderStatusEnum;
import com.fu.yygh.model.order.OrderInfo;
import com.fu.yygh.orders.service.OrderInfoService;
import com.fu.yygh.orders.utils.AuthContextHolder;
import com.fu.yygh.vo.order.OrderCountQueryVo;
import com.fu.yygh.vo.order.OrderCountVo;
import com.fu.yygh.vo.order.OrderQueryVo;
import com.google.common.collect.Ordering;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 前端控制器
 * </p>
 *
 * @author fu
 * @since 2023-07-07
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderInfoController {

    @Autowired
    private OrderInfoService orderInfoService;


    @PostMapping("/countOrderInfoByQuery")
    public Map<String,Object> countOrderInfoByQuery(@RequestBody OrderCountQueryVo orderCountQueryVo){
        return orderInfoService.countOrderInfoByQuery(orderCountQueryVo);
    }


    //取消预约
    @GetMapping("auth/cancelOrder/{orderId}")
    public R cancelOrder(@PathVariable("orderId")Long orderId){
        Boolean flag=orderInfoService.cancelOrder(orderId);
        return R.ok().data("flag",flag);
    }



    //订单列表
    @GetMapping("auth/{page}/{limit}")
    public R list(@PathVariable("page")Integer page,
                  @PathVariable("limit")Integer limit,
                  OrderQueryVo orderQueryVo, HttpServletRequest request){
        //设置当前用户id
        Long userId = AuthContextHolder.getUserId(request);
        orderQueryVo.setUserId(userId);

        Page<OrderInfo> pageParam=new Page<>(page,limit);
        IPage<OrderInfo> pageModel=orderInfoService.selectPage(pageParam,orderQueryVo);
        return R.ok().data("pageModel",pageModel);
    }

    //获取订单状态
    @GetMapping("auth/getStatusList")
    public R getStatusList(){
        return R.ok().data("statusList", OrderStatusEnum.getStatusList());
    }


    @ApiOperation(value = "创建订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public R saveOrder(@PathVariable("scheduleId")String scheduleId,
                       @PathVariable("patientId")Integer patientId){
        Long orderId=orderInfoService.saveOrder(scheduleId,patientId);
        return R.ok().data("orderId",orderId);
    }

    //根据订单id查询订单详情
    @GetMapping("auth/getOrders/{orderId}")
    public R getOrders(@PathVariable("orderId")Long orderId){
        OrderInfo orderInfo=orderInfoService.getOrders(orderId);
        return R.ok().data("orderInfo",orderInfo);
    }
}

