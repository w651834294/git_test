package com.fu.yygh.hosp.service;

import com.fu.yygh.model.hosp.Schedule;
import com.fu.yygh.vo.hosp.ScheduleOrderVo;
import com.fu.yygh.vo.hosp.ScheduleQueryVo;
import com.fu.yygh.vo.order.OrderMqVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface ScheduleService {
    void save(Map<String, Object> stringObjectMap);

    Page<Schedule> selectPage(int page, int limit, ScheduleQueryVo scheduleQueryVo);

    void removeScheduleByHospIdAndSheId(Map<String, Object> paramMap);

    Map<String, Object> getSchedulePage(Integer page, Integer limit, String hoscode, String depcode);

    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    Map<String, Object> getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode);

    Schedule getScheduleList(String scheduleId);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    void update(OrderMqVo orderMqVo);
}
