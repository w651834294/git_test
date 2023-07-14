package com.fu.yygh.user.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fu.yygh.common.exception.YYGHException;
import com.fu.yygh.common.utils.JwtHelper;
import com.fu.yygh.enums.AuthStatusEnum;
import com.fu.yygh.model.user.Patient;
import com.fu.yygh.model.user.UserInfo;
import com.fu.yygh.user.mapper.UserInfoMapper;
import com.fu.yygh.user.service.PatientService;
import com.fu.yygh.user.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fu.yygh.vo.user.LoginVo;
import com.fu.yygh.vo.user.UserAuthVo;
import com.fu.yygh.vo.user.UserInfoQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author fu
 * @since 2023-06-30
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {


    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @Autowired
    private PatientService patientService;

    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        //1.获取用户输入的手机号和验证码
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //2.对用户输入的手机号和验证码进行非空校验
        if(StringUtils.isEmpty(phone)||StringUtils.isEmpty(code)){
            throw new YYGHException(20001,"用户名和验证码不能为空");
        }
        //3.和redis中的验证码进行比较
        String redisCode =redisTemplate.opsForValue().get(phone);
        if(!code.equals(redisCode)){
            throw new YYGHException(20001,"验证码错误");
        }


        String openid = loginVo.getOpenid();
        UserInfo userInfo=null;
        if(StringUtils.isEmpty(openid)){
            //为空表示用的是手机号登陆
            QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<UserInfo>();
            queryWrapper.eq("phone",phone);
            userInfo = baseMapper.selectOne(queryWrapper);
            if(userInfo==null){//4.判断是不是首次登录，如果是就自动注册
                userInfo=new UserInfo();
                userInfo.setPhone(phone);
                userInfo.setStatus(1);
                userInfo.setCreateTime(new Date());
                baseMapper.insert(userInfo);
            }
        }else {
            //不为空表示微信绑定手机号
            UserInfo userInfoFinal=new UserInfo();
            QueryWrapper<UserInfo> phoneQueryWrapper=new QueryWrapper<UserInfo>();
            phoneQueryWrapper.eq("phone",phone);
            UserInfo userInfoByPhone = baseMapper.selectOne(phoneQueryWrapper);
            if(userInfoByPhone!=null){
                //不为空说明用手机号登陆过
                BeanUtils.copyProperties(userInfoByPhone,userInfoFinal);
                baseMapper.delete(phoneQueryWrapper);
            }else {
                userInfoFinal.setPhone(phone);
            }

            QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<UserInfo>();
            queryWrapper.eq("openid",openid);
            userInfo = baseMapper.selectOne(queryWrapper);


            userInfoFinal.setId(userInfo.getId());
            userInfoFinal.setNickName(userInfo.getNickName());
            userInfoFinal.setOpenid(userInfo.getOpenid());
            userInfoFinal.setStatus(userInfo.getStatus());


            baseMapper.updateById(userInfoFinal);
        }

        //5.判断用户状态
        Integer status = userInfo.getStatus();
        if(status==0){
            throw new YYGHException(20001,"该用户已被禁用");
        }

        //6.返回用户信息
        Map<String,Object> map=new HashMap<String,Object>();
        JwtHelper.createToken(userInfo.getId(),userInfo.getName());
        map.put("token","");//todo
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)){
            name=userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)){
            name=userInfo.getPhone();
        }
        map.put("name",name);

        String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
        map.put("token",token);
        return map;
    }

    @Override
    public UserInfo selectByOpenId(String openid) {
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<UserInfo>();
        queryWrapper.eq("openid",openid);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        return userInfo;
    }

    @Override
    public UserInfo selectById(Long userId) {
        if(userId==null){
            return null;
        }
        QueryWrapper<UserInfo> queryWrapper=new QueryWrapper<UserInfo>();
        queryWrapper.eq("id",userId);
        UserInfo userInfo = baseMapper.selectOne(queryWrapper);
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        return userInfo;
    }

    @Override
    public void saveUserAuth(Long userId, UserAuthVo userAuthVo) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(userId);
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        baseMapper.updateById(userInfo);
    }

    @Override
    public Page<UserInfo> selectUserInfoPage(Integer page, Integer limit, UserInfoQueryVo userInfoQueryVo) {
        Page<UserInfo> pageInfo = new Page<UserInfo>(page, limit);
        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<UserInfo>();
        //userInfoQueryVo获取条件值
        //用户名称
        String name = userInfoQueryVo.getKeyword();
        //用户状态
        Integer status = userInfoQueryVo.getStatus();
        //认证状态
        Integer authStatus = userInfoQueryVo.getAuthStatus();
        //开始时间
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();
        //结束时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();
        //对获取到的条件之进行非空判断
        if (!StringUtils.isEmpty(name)) {
            queryWrapper.like("name", name).or().eq("phone",name);
        }
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.eq("status", status);
        }
        if (!StringUtils.isEmpty(authStatus)) {
            queryWrapper.eq("auth_status", authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            queryWrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            queryWrapper.le("create_time",createTimeEnd);
        }

        Page<UserInfo> userInfoPage = baseMapper.selectPage(pageInfo, queryWrapper);

        userInfoPage.getRecords().parallelStream().forEach(item->{
            this.packgeUserInfo(item);
        });
        return userInfoPage;
    }

    @Override
    public void lock(Long userId, Integer status) {
        if(status==0||status==1){
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userId);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> show(Long userId) {
        Map<String,Object> map=new HashMap<>();
        //根据用户id查询用户信息
        UserInfo userInfo=this.packgeUserInfo(baseMapper.selectById(userId));
        map.put("userInfo",userInfo);
        //根据用户id查询下面的所有就诊人信息
        List<Patient> patientList=patientService.getPatientListByUid(userId);
        map.put("patientList",patientList);
        return map;
    }

    @Override
    public void approval(Long userId, Integer authStatus) {
        if(authStatus==-1||authStatus==2){
            //根据用户id查询当前用户信息
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(authStatus);
            baseMapper.updateById(userInfo);
        }
    }


    //编号变成对应的值进行封装
    private UserInfo packgeUserInfo(UserInfo userInfo){
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        String statusString=userInfo.getStatus().intValue()==0?"锁定":"正常";
        userInfo.getParam().put("statusString",statusString);
        return userInfo;
    }
}
