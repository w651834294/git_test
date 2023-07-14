package com.fu.yygh.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.fu.yygh.common.result.R;
import com.fu.yygh.common.utils.JwtHelper;
import com.fu.yygh.model.user.UserInfo;
import com.fu.yygh.user.service.UserInfoService;
import com.fu.yygh.user.utils.ConstantPropertiesUtil;
import com.fu.yygh.user.utils.HttpClientUtils;
import com.google.gson.Gson;
import com.sun.org.apache.bcel.internal.generic.NEW;
import io.swagger.annotations.ApiOperation;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/api/ucenter/wx")
public class WeChatController {


    @Autowired
    private UserInfoService userInfoService;


    @GetMapping("callback")
    public String callback(String code,String state){
        System.out.println(code+"--------"+state);
        //第一种写法：String accessTokenUrl="https://api.weixin.qq.com/sns/oauth2/access_token?appid="+ConstantPropertiesUtil.WX_OPEN_APP_ID+"&secret=SECRET&code=CODE&grant_type=authorization_code"
        //第二种写法：
        StringBuilder stringBuilder=new StringBuilder();
        StringBuilder str = stringBuilder.append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String format = String.format(str.toString(), ConstantPropertiesUtil.WX_OPEN_APP_ID, ConstantPropertiesUtil.WX_OPEN_APP_SECRET, code);

        try {
            String jsonStr = HttpClientUtils.get(format);
            //第一种方法
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");

            //第二种方法
//            Gson gson = new Gson();
//            Map map = gson.fromJson(jsonStr, Map.class);
//            Object access_token1 = map.get("access_token");
//            Object openid1 = map.get("openid");

            UserInfo userInfo=userInfoService.selectByOpenId(openid);

            if(null==userInfo){
                //没有说明是首次登录，进行自动注册
                userInfo = new UserInfo();

                /*String baseUserInfoUrl="https://api.weixin.qq.com/sns/userinfo"+
                        "?access_token=%s"+"&openid=%s";
                String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);*/
                StringBuilder append = new StringBuilder().append("https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token)
                        .append("&openid=" + openid);

                String userInfoStr = HttpClientUtils.get(append.toString());


                JSONObject jsonObject1 = JSONObject.parseObject(userInfoStr);
                String nickname = jsonObject1.getString("nickname");


                userInfo.setNickName(nickname);
                userInfo.setOpenid(openid);
                userInfo.setStatus(1);
                userInfoService.save(userInfo);
            }

            //给前端返回用户信息：name,token
            String name = userInfo.getName();
            Map<String,Object> map=new HashMap<String,Object>();

            if(StringUtils.isEmpty(name)){
                name = userInfo.getNickName();
            }
            if(StringUtils.isEmpty(name)){
                name = userInfo.getPhone();
            }
            map.put("name",name);
            String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
            map.put("token",token);


            //再返回openid
            //根据openid查询出来的userinfo中的手机号是否为空，如果为空，说明微信和手机号没有绑定过，返回openid
            //前端拿到不为空的openid，说明需要做手机号绑定
            if(StringUtils.isEmpty(userInfo.getPhone())){
                map.put("openid",openid);
            }else {
                map.put("openid","");//如果为空，表示已经绑定手机号
            }


            return "redirect:http://localhost:3000/weixin/callback?token="+map.get("token")+ "&openid="+map.get("openid")+"&name="+URLEncoder.encode((String) map.get("name"),"utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    @ApiOperation(value = "获取微信登陆参数")
    @GetMapping("getLoginParam")
    @ResponseBody //返回json数据
    public R getQrConnect() throws UnsupportedEncodingException {
        String redirectUri = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "UTF-8");
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
        map.put("redirectUri",redirectUri);
        map.put("scope","snsapi_login");
        map.put("state",System.currentTimeMillis()+"");
        return R.ok().data(map);
    }



}
