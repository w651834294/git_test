package com.fu.yygh.user.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fu.yygh.common.result.R;
import com.fu.yygh.model.user.UserInfo;
import com.fu.yygh.user.service.UserInfoService;
import com.fu.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/userinfo")
public class UserController {

    @Autowired
    private UserInfoService userInfoService;

    //带条件查询的分页
    @GetMapping("{page}/{limit}")
    public R getUserInfoPage(@PathVariable(value = "page")Integer page,
                             @PathVariable(value = "limit")Integer limit,
                             UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> pageModel =userInfoService.selectUserInfoPage(page,limit,userInfoQueryVo);
        return R.ok().data("pageModel",pageModel);
    }

    //锁定
    @GetMapping("lock/{userId}/{status}")
    public R lock(@PathVariable("userId")Long userId,@PathVariable("status")Integer status){
        userInfoService.lock(userId,status);
        return R.ok();
    }

    //根据用户id查询用户详细信息
    @GetMapping("show/{userId}")
    public R show(@PathVariable("userId")Long userId){
       Map<String,Object> map=userInfoService.show(userId);
       return R.ok().data(map);
    }

    //审批
    @GetMapping("approval/{userId}/{authStatus}")
    public R approval(@PathVariable("userId")Long userId,@PathVariable("authStatus")Integer authStatus){
        userInfoService.approval(userId,authStatus);
        return R.ok();
    }
}
