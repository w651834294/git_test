package com.fu.yygh.hospital.client;

import com.fu.yygh.vo.hosp.ScheduleOrderVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("service-hosp")
public interface HospitalFeignClient {
    //根据排班id查询排班信息
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId")String scheduleId);
}
