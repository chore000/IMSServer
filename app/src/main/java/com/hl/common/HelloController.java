package com.hl.common;

import com.alibaba.dingtalk.openapi.demo.auth.AuthHelper;
import com.hl.Intercepetor.LoginInterceptor;
import com.hl.util.MD5Helper;
import com.jfinal.aop.Clear;
import com.jfinal.core.Controller;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import org.apache.log4j.Logger;

/**
 * Created by dell on 2017/2/16.
 * 7181项目首页，返回验证信息
 */
public class HelloController extends Controller {
    Logger log = Logger.getLogger(HelloController.class);
    static CommonServices commonServices = new CommonServices();
    static Cache userredis = Redis.use("userredis");

    @Clear(LoginInterceptor.class)
    public void getconfig() {
        String Conf = AuthHelper.getConfig(getRequest());
        renderText(Conf);
    }

    @Clear(LoginInterceptor.class)
    public void login() {
        log.info("开始登陆");
        String code = getPara("code");
        String corpId = getPara("corpid");
        String access_token = getPara("access_token");
        System.out.println("code:" + code + " corpid:" + corpId);
        access_token = commonServices.loginbycode(code, corpId, access_token);
        log.info("验证完成");
        renderText(access_token);
    }

    /**
     * 获取用户信息
     */
    public void userinfo() {
        log.info("接收请求:");
        String access_token = getPara("access_token");
        String userjsons = userredis.get(access_token);
        String s = null;
        try {
            s = MD5Helper.KL(userjsons);
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderText(s);

    }

    @Clear(LoginInterceptor.class)
    public void updateuseinfo() {
        log.info("更新用户信息");
        try {
            String access_token = getPara("access_token");
            String userjsons = userredis.get(access_token);
        } catch (Exception e) {
            log.error(e);
        }

        commonServices.updatedept();
    }

}
