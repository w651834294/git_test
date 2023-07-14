package com.fu.yygh.hosp.controller;

import com.fu.yygh.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/yygh/user")
@Slf4j
//@CrossOrigin //允许跨域

public class UserController {

    @PostMapping("/login")
    public R login(){
        return R.ok().data("token","admin-token");
    }


    @GetMapping("/info")
    public R info(String token){
        System.out.println(token);
        return R.ok()
                .data("roles","[admin]")
                .data("introduction","I am a super administrator")
                .data("avatar","https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif")
                .data("name","Super Admin");
    }
}





