package com.fu.yygh.orders.service;

import java.util.Map;

public interface WeChatService {
    Map<String, Object> createNative(Long orderId);

    Map<String, String> queryPayStatus(Long orderId);

    boolean refund(Long orderId);
}
