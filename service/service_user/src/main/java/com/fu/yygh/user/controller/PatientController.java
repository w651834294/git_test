package com.fu.yygh.user.controller;


import com.fu.yygh.cmn.client.DictFeignClient;
import com.fu.yygh.common.result.R;
import com.fu.yygh.model.user.Patient;
import com.fu.yygh.user.service.PatientService;
import com.fu.yygh.user.utils.AuthContextHolder;
import com.fu.yygh.user.utils.ConstantPropertiesUtil;
import com.fu.yygh.user.utils.HttpClientUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 就诊人表 前端控制器
 * </p>
 *
 * @author fu
 * @since 2023-07-05
 */
@Api(tags = "就诊人管理接口")
@RestController
@RequestMapping("/api/userinfo/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DictFeignClient dictFeignClient;

    @ApiOperation(value = "添加就诊人信息")
    @PostMapping("auth/save")
    public R savePatient(@RequestBody Patient patient, HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return R.ok();
    }

    @ApiOperation(value = "删除就诊人信息")
    @DeleteMapping("auth/remove/{id}")
    public R removeById(@PathVariable("id")Integer id){
        patientService.removeById(id);
        return R.ok();
    }

    //根据id获取就诊人信息，修改就诊人信息是回显数据
    @ApiOperation(value = "修改就诊人信息")
    @GetMapping("auth/get/{id}")
    public R getPatientInfo(@PathVariable("id")Integer id){
        Patient patient=patientService.getPatientInfo(id);
        return R.ok().data("patient",patient);
    }

    //修改就诊人信息
    @PutMapping("auth/update")
    public R updatePatient(@RequestBody Patient patient){
        patientService.updateById(patient);
        return R.ok();
    }

    //查询当前登录用户底下的所有的就诊人信息
    @GetMapping("auth/findAll")
    public R getPatientList(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        List<Patient> list=patientService.getPatientListByUid(userId);
        return R.ok().data("list",list);
    }
}

