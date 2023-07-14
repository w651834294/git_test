package com.fu.yygh.user.client;

import com.fu.yygh.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-user")
public interface PatientFeignClient {
    //获取就诊人信息
    @GetMapping("/api/userinfo/patient/auth/get/{id}")
    public R getPatientInfo(@PathVariable("id")Integer id);
}
