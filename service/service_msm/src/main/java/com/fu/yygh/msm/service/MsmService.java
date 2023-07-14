package com.fu.yygh.msm.service;

import com.fu.yygh.vo.msm.MsmVo;

public interface MsmService {
    boolean sendCode(String phone);

    void send(MsmVo msmVo);
}
