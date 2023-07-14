package com.fu.yygh.hosp.controller.api;

import com.fu.yygh.common.result.R;
import com.fu.yygh.hosp.service.DepartmentService;
import com.fu.yygh.hosp.service.HospitalService;
import com.fu.yygh.hosp.service.ScheduleService;
import com.fu.yygh.hosp.service.impl.DepartmentServiceImpl;
import com.fu.yygh.model.hosp.Hospital;
import com.fu.yygh.model.hosp.Schedule;
import com.fu.yygh.vo.hosp.DepartmentVo;
import com.fu.yygh.vo.hosp.HospitalQueryVo;
import com.fu.yygh.vo.hosp.ScheduleOrderVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "医院用户接口")
@RestController
@RequestMapping("/api/hosp/hospital")
public class HospitalUserPageController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;


    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId")String scheduleId){
        return scheduleService.getScheduleOrderVo(scheduleId);
    }




    //根据排班id查询排班信息
    @ApiOperation(value = "获取排班详情")
    @GetMapping("getSchedule/{id}")
    public R getScheduleList(@PathVariable("id")String scheduleId){
        Schedule schedule=scheduleService.getScheduleList(scheduleId);
        return R.ok().data("schedule",schedule);
    }


    @ApiOperation(value = "获取排班数据")
    @GetMapping("auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public R findScheduleList(@PathVariable("hoscode")String hoscode,
                              @PathVariable("depcode")String depcode,
                              @PathVariable("workDate")String workDate){
        List<Schedule> scheduleList = scheduleService.getScheduleDetail(hoscode, depcode, workDate);
        return R.ok().data("scheduleList",scheduleList);
    }



    //获取排班信息
    @ApiOperation(value = "获取可预约的排班数据")
    @GetMapping("auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public R getBookingSchedule(@PathVariable("page")Integer page,
                                @PathVariable("limit")Integer limit,
                                @PathVariable("hoscode")String hoscode,
                                @PathVariable("depcode")String depcode){
        Map<String,Object> map =scheduleService.getBookingSchedule(page,limit,hoscode,depcode);
        return R.ok().data(map);
    }






    @ApiOperation(value = "根据医院编号查询医院信息")
    @GetMapping("/info/{hoscode}")
    public R info(@ApiParam(name = "hoscode",value = "医院编号",required = true)@PathVariable("hoscode")String hoscode){
        Hospital byHoscode = hospitalService.getByHoscode(hoscode);
        return R.ok().data("hospital",byHoscode);
    }


    @ApiOperation(value = "根据医院编号查询当前医院下所有的科室信息")
    @GetMapping("/department/list/{hoscode}")
    public R getDepartmentListByHoscode(@ApiParam(name = "hoscode",value = "医院编号",required = true)@PathVariable("hoscode")String hoscode){
        List<DepartmentVo> deptList = departmentService.getDeptList(hoscode);
        return R.ok().data("list",deptList);
    }

    @ApiOperation(value = "获取带查询条件的首页医院列表")
    @GetMapping("/{page}/{limit}")
    public R getHospitalPage(@ApiParam(name = "page",value = "当前页",required = true) @PathVariable("page")Integer page,
                             @ApiParam(name = "limit",value = "每页显示的条数",required =true)
                             @PathVariable("limit")Integer limit,
                             @ApiParam(name = "queryVo",value = "查询条件",required = false)
                             HospitalQueryVo hospitalQueryVo){
        Page<Hospital> hospitals = hospitalService.selectPage(page, limit, hospitalQueryVo);
        return R.ok().data("pages",hospitals);
    }


    @ApiOperation(value = "根据医院名称进行模糊查询，查询所有符合条件的医院信息")
    @GetMapping("/findByNameLike/{hosname}")
    public R findByNameLike(@ApiParam(name = "hosName",value = "医院名称",required = true)@PathVariable("hosname")String hosname){
        List<Hospital> list=hospitalService.findByNameLike(hosname);
        return R.ok().data("list",list);
    }
}
