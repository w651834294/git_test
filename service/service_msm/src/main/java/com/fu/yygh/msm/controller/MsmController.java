package com.fu.yygh.msm.controller;

import com.fu.yygh.common.result.R;
import com.fu.yygh.msm.service.MsmService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/msm")
public class MsmController {

    @Autowired
    private MsmService msmService;

    @ApiOperation(value = "发送手机号")
    @PostMapping("/send/{phone}")
    public R sendCode(@ApiParam(name = "phone",value = "手机号",required = true) @PathVariable("phone")String phone){
            boolean flag=msmService.sendCode(phone);
            if(flag){
                return R.ok();
            }else {
                return R.error().message("验证码发送失败");
            }
    }
}
