package com.fu.yygh.hosp.controller;

import com.fu.yygh.common.result.R;
import com.fu.yygh.hosp.service.DepartmentService;
import com.fu.yygh.model.hosp.Department;
import com.fu.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/hosp/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    //根据医院编号查询该医院底下所有的科室信息，返回所有大科室的数据
    @ApiOperation(value = "查询医院所有科室信息")
    @GetMapping("getDeptList/{hoscode}")
    public R getDeptList(@ApiParam(name = "hoscode",value = "医院编号",required = true) @PathVariable("hoscode")String hoscode){
        List<DepartmentVo> departmentVoList=departmentService.getDeptList(hoscode);
        return R.ok().data("list",departmentVoList);
    }
}
