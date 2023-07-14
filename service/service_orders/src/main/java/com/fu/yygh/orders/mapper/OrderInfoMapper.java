package com.fu.yygh.orders.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fu.yygh.model.order.OrderInfo;
import com.fu.yygh.vo.order.OrderCountQueryVo;
import com.fu.yygh.vo.order.OrderCountVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author fu
 * @since 2023-07-07
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    List<OrderCountVo> countOrderInfoByQuery(OrderCountQueryVo orderCountQueryVo);
}
