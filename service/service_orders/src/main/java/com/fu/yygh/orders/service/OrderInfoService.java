package com.fu.yygh.orders.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fu.yygh.model.order.OrderInfo;
import com.fu.yygh.vo.order.OrderCountQueryVo;
import com.fu.yygh.vo.order.OrderCountVo;
import com.fu.yygh.vo.order.OrderQueryVo;
import com.google.common.collect.Ordering;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 服务类
 * </p>
 *
 * @author fu
 * @since 2023-07-07
 */
public interface OrderInfoService extends IService<OrderInfo> {

    Long saveOrder(String scheduleId, Integer patientId);

    IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryVo orderQueryVo);

    OrderInfo getOrders(Long orderId);

    Boolean cancelOrder(Long orderId);

    void patientTips();

    Map<String,Object> countOrderInfoByQuery(OrderCountQueryVo orderCountQueryVo);
}
