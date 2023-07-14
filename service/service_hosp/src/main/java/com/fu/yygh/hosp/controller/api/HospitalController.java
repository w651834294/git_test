package com.fu.yygh.hosp.controller.api;

import com.fu.yygh.common.exception.YYGHException;
import com.fu.yygh.common.result.R;
import com.fu.yygh.hosp.service.DepartmentService;
import com.fu.yygh.hosp.service.HospitalService;
import com.fu.yygh.hosp.service.HospitalSetService;
import com.fu.yygh.hosp.service.ScheduleService;
import com.fu.yygh.hosp.util.HttpRequestHelper;
import com.fu.yygh.hosp.util.MD5;
import com.fu.yygh.model.hosp.Hospital;
import com.fu.yygh.model.hosp.Schedule;
import com.fu.yygh.result.Result;
import com.fu.yygh.vo.hosp.HospitalSetQueryVo;
import com.fu.yygh.vo.hosp.ScheduleQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/api/hosp")
@Api(tags = "医院管理API接口")
public class HospitalController {

    @Autowired
    private HospitalService hospitalService;

    @Autowired
    private HospitalSetService hospitalSetService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private ScheduleService scheduleService;

    @ApiOperation(value = "根据医院id和排班id删除排班信息")
    @PostMapping("schedule/remove")
    public Result removeScheduleByHospIdAndScheId(HttpServletRequest request){
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        scheduleService.removeScheduleByHospIdAndSheId(paramMap);
        return Result.ok();
    }

    @ApiOperation(value = "查询排班分页信息")
    @PostMapping("schedule/list")
    public Result getSchedulePage(HttpServletRequest request){
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) paramMap.get("hoscode");
        String depcode = (String) paramMap.get("depcode");

        int page=StringUtils.isEmpty(paramMap.get("page"))?1:Integer.parseInt((String)paramMap.get("page"));
        int limit=StringUtils.isEmpty(paramMap.get("limit"))?10:Integer.parseInt((String)paramMap.get("limit"));

        ScheduleQueryVo scheduleQueryVo = new ScheduleQueryVo();
        scheduleQueryVo.setHoscode(hoscode);
        scheduleQueryVo.setDepcode(depcode);
        Page<Schedule> pageModel=scheduleService.selectPage(page,limit,scheduleQueryVo);
        return Result.ok(pageModel);
    }



    @ApiOperation(value = "上传排班")
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());

//        String hoscode = (String) stringObjectMap.get("hoscode");
        scheduleService.save(stringObjectMap);
        return Result.ok();

    }



    @ApiOperation(value = "删除科室")
    @RequestMapping("department/remove")
    public Result removeDepartment(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());
        String hoscode = (String) stringObjectMap.get("hoscode");
        String depcode = (String) stringObjectMap.get("depcode");

        departmentService.remove(hoscode,depcode);
        return Result.ok();

    }



    @ApiOperation(value = "获取分页列表")
    @RequestMapping("department/list")
    public Result getDepartmentPage(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());

        Page page=departmentService.findDepartmentPage(stringObjectMap);

        return Result.ok(page);
    }


    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public R index(Integer page, Integer limit, HospitalSetQueryVo hospitalSetQueryVo){
        return R.ok().data("pages",hospitalService.selectPage(page,limit,hospitalSetQueryVo));
    }


    @ApiOperation(value = "上传科室信息")
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());


        departmentService.saveDepartment(stringObjectMap);
        return Result.ok();
    }


    @ApiOperation(value = "查询医院信息")
    @PostMapping("hospital/show")
    public Result show(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());

        String hoscode = (String) stringObjectMap.get("hoscode");
        if(StringUtils.isEmpty(hoscode)){
            throw new YYGHException(20001,"失败");
        }

        String sign = (String) stringObjectMap.get("sign");
        String platFormSignKey=hospitalSetService.getSignKey((String)stringObjectMap.get("hoscode"));
        Hospital hospital;

        if(StringUtils.isEmpty(sign)&& StringUtils.isEmpty(platFormSignKey)&&sign.equals(MD5.encrypt(platFormSignKey))){
            throw new YYGHException(20001,"校验失败");
        }else {
            hospital=hospitalService.getByHoscode(hoscode);
        }

        return Result.ok(hospital);
    }




    @ApiOperation(value = "上传医院")
    @PostMapping("/saveHospital")
    public Result saveHospital(HttpServletRequest request){
        Map<String, Object> stringObjectMap = HttpRequestHelper.switchMap(request.getParameterMap());

        //数据合法校验
        String sign = (String) stringObjectMap.get("sign");
        String platFormSignKey=hospitalSetService.getSignKey((String)stringObjectMap.get("hoscode"));

        if(!StringUtils.isEmpty(sign)&& !StringUtils.isEmpty(platFormSignKey)&&MD5.encrypt(platFormSignKey).equals(sign)){
            hospitalService.save(stringObjectMap);
        }else {
            throw new YYGHException(20001,"校验失败");
        }



        return Result.ok(); //TODO 待完善
    }



}
