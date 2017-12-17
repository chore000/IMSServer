package com.hl.Task;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class SecTaskController extends Controller {
    private static SecTaksServiece secTaksServiece = new SecTaksServiece();
    Cache redis = Redis.use("userredis");

    public void addsectask() {
        String taskjson = getPara("taskinfo");
        String taskid = getPara("taskid");
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String stat = getPara("stat");
        String userid = jb.getString("userid");
        boolean res = secTaksServiece.add2ndtaskbyjson(taskjson, userid, taskid);
        renderText(res + "");
    }

    public void getmarksdiv() {
        int taskid = getParaToInt("taskid");
        String markdiv = secTaksServiece.gettask(taskid);
        renderText(markdiv);
    }
}
