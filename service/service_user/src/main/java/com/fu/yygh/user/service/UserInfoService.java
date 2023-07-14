package com.fu.yygh.user.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fu.yygh.model.user.UserInfo;
import com.fu.yygh.vo.user.LoginVo;
import com.fu.yygh.vo.user.UserAuthVo;
import com.fu.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author fu
 * @since 2023-06-30
 */
public interface UserInfoService extends IService<UserInfo> {

    Map<String, Object> login(LoginVo loginVo);

    UserInfo selectByOpenId(String openid);

    UserInfo selectById(Long userId);

    void saveUserAuth(Long userId, UserAuthVo userAuthVo);

    Page<UserInfo> selectUserInfoPage(Integer page, Integer limit, UserInfoQueryVo userInfoQueryVo);

    void lock(Long userId, Integer status);

    Map<String, Object> show(Long userId);

    void approval(Long userId, Integer authStatus);
}
