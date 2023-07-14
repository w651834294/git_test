package com.fu.yygh.hosp.controller;

import com.fu.yygh.common.result.R;
import com.fu.yygh.hosp.service.HospitalService;
import com.fu.yygh.model.hosp.Hospital;
import com.fu.yygh.result.Result;
import com.fu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "医院信息接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
@Slf4j
//@CrossOrigin
public class HospitalPlatController {
    @Autowired
    private HospitalService hospitalService;

    //医院列表
    @ApiOperation(value = "带条件查询医院分页信息")
    @GetMapping("{page}/{limit}")
    public R getHospitalPage(@ApiParam(name = "page",value = "当前页页码",required = true) @PathVariable("page")Integer page,
                           @ApiParam(name = "limit",value = "每页显示条数",required = true)@PathVariable("limit")Integer limit,
                           @ApiParam(name = "hospitalQueryVo",value = "查询对象",required = false)HospitalQueryVo hospitalQueryVo){

        Page<Hospital> pageObj = hospitalService.selectPage(page, limit, hospitalQueryVo);
        return R.ok().data("pages",pageObj);
    }

    //更新医院上线或下线状态
    @ApiOperation(value = "更新医院上线或下线状态")
    @PutMapping("/updateStatus/{id}/{status}")
    public R updateStatus(@ApiParam(name = "id",value = "医院id",required = true)@PathVariable("id")String id,
                          @ApiParam(name = "status",value = "状态",required = true)@PathVariable("status")Integer status){
        hospitalService.update(id,status);
        return R.ok();
    }

    //根据医院id获取医院详情信息
    @ApiOperation(value = "根据医院id获取医院详情信息")
    @GetMapping("/detail/{id}")
    public R getHospitalDeatilById(@ApiParam(name = "id",value = "医院id",required = true)@PathVariable("id")String id){
        Map<String,Object> map=hospitalService.getHospitalDeatilById(id);
        return R.ok().data("hospital",map);
    }
}
