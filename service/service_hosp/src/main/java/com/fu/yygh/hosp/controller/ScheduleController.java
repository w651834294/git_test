package com.fu.yygh.hosp.controller;

import com.fu.yygh.common.result.R;
import com.fu.yygh.hosp.service.ScheduleService;
import com.fu.yygh.model.hosp.Schedule;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/hosp/schedule")
public class ScheduleController {


    @Autowired
    private ScheduleService scheduleService;

    //根据医院编号，科室编号和工作日期，查询排班详细信息
    @ApiOperation(value = "查询排班详细信息")
    @GetMapping("getScheduleDetail/{hoscode}/{depcode}/{workDate}")
    public R getScheduleDetail(
            @ApiParam(name = "hoscode",value = "医院编号",required = true)
            @PathVariable("hoscode")String hoscode,
            @ApiParam(name = "depcode",value = "科室编号",required = true)
            @PathVariable("depcode")String depcode,
            @ApiParam(name = "workDate",value = "工作日期",required = true)
            @PathVariable("workDate")String workDate){
        List<Schedule> list =scheduleService.getScheduleDetail(hoscode,depcode,workDate);
        return R.ok().data("list",list);
    }



    //根据医院编号和科室编号，查询排版规则数据
    @ApiOperation(value = "查询排班规则数据")
    @GetMapping("getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getSchedulePage(
                            @ApiParam(name = "page",value = "当前页",required = true)
                            @PathVariable("page")Integer page,
                            @ApiParam(name = "limit",value = "每页显示的条数",required = true)
                            @PathVariable("limit")Integer limit,
                            @ApiParam(name = "hoscode",value = "医院编号",required = true)
                            @PathVariable("hoscode")String hoscode,
                            @ApiParam(name = "depcode",value = "科室编号",required = true)
                            @PathVariable("depcode")String depcode){

        Map<String,Object> map =scheduleService.getSchedulePage(page,limit,hoscode,depcode);
        return R.ok().data(map);
    }
}
