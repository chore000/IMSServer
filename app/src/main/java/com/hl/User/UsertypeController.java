package com.hl.User;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class UsertypeController extends Controller {
    private static UserService userService = new UserService();
    Cache redis = Redis.use("userredis");

    public void updateusertype() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String lvl = getPara("lvl");
        String type = getPara("type");
        String res = userService.updatetasktype(assignee, type, lvl);
        renderText(res);
    }

    public void getusertype() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String res = userService.gettasktype(assignee);
        renderText(res);
    }
}
