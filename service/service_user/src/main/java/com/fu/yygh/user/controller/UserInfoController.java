package com.fu.yygh.user.controller;


import com.fu.yygh.common.result.R;
import com.fu.yygh.common.utils.JwtHelper;
import com.fu.yygh.model.user.UserInfo;
import com.fu.yygh.user.service.UserInfoService;
import com.fu.yygh.user.utils.AuthContextHolder;
import com.fu.yygh.user.utils.ConstantPropertiesUtil;
import com.fu.yygh.vo.acl.UserQueryVo;
import com.fu.yygh.vo.user.LoginVo;
import com.fu.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author fu
 * @since 2023-06-30
 */
@RestController //把返回的数据转换成json数据
@RequestMapping("/api/userinfo")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;


    //保存用户认证信息
    @ApiOperation(value = "用户认证")
    @PostMapping("auth/userAuth")
    public R saveUserAuth(@RequestBody UserAuthVo userAuthVo,HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        userInfoService.saveUserAuth(userId,userAuthVo);
        return R.ok();
    }



    //根据用户id获取用户信息
    @ApiOperation(value = "获取用户信息")
    @GetMapping("auth/getUserInfo")
    public R getUserInfo(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        //UserInfo userInfo = userInfoService.getById(userId);
        UserInfo userInfo=userInfoService.selectById(userId);
        return R.ok().data("userInfo",userInfo);
    }



    @ApiOperation(value = "会员登录")
    @PostMapping("/login")
    public R login(@RequestBody LoginVo loginVo){
        Map<String,Object> map=userInfoService.login(loginVo);
        return R.ok().data(map);
    }
}

