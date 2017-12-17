package com.hl.Intercepetor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class LoginInterceptor implements Interceptor {
Cache redis= Redis.use("userredis");
    @Override
    public void intercept(Invocation inv) {

        String accesstoken=inv.getController().getCookie("access_token");
//        System.out.println(accesstoken);
        if (accesstoken!=null&&redis.get(accesstoken)!=null){
            inv.invoke();
        }
        else
        {
            inv.getController().renderText("无权限");
        }
    }
}
