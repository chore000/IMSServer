package com.hl.taskaplly;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class TaskApplyController extends Controller {
    private static TaskApplyService taskApplyService = new TaskApplyService();
    Cache redis = Redis.use("userredis");

    public void index() {
        renderText("ok");
    }

    /***
     * 发起任务申请
     */
    public void taskapply() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String stat = getPara("stat");
        String userid = jb.getString("userid");
        String taskapply = getPara("taskapply");
        boolean res = taskApplyService.addTaskapply(taskapply, userid, stat);
        renderText(res + "");
    }

    /**
     * 获取我的审批列表
     */
    public void getmyaprove() {

        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String userid = jb.getString("userid");

        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "10");

        String res = taskApplyService.getTaskapplytodolist(pagenum, pagesize, userid);
        renderText(res);
    }

    /**
     * 获取我的任务申请
     */
    public void getmyapply() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String userid = jb.getString("userid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "10");

        String res = taskApplyService.getTaskapplymysendlist(pagenum, pagesize, userid);
        renderText(res);
    }

    /**
     * 同意任务申请
     */
    public void agreeapply() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String userid = jb.getString("userid");
        String applyid = getPara("applyid");
        boolean res = taskApplyService.aprovetask(applyid, userid);
        renderText(res + "");
    }

    /**
     * 拒绝任务申请
     */
    public void rejectapply() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String userid = jb.getString("userid");
        String applyid = getPara("applyid");
        boolean res = taskApplyService.rejecttask(applyid, userid);
        renderText(res + "");
    }

    /**
     * 终止任务申请
     */
    public void stopapply() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String userid = jb.getString("userid");
        String applyid = getPara("applyid");
        String res = taskApplyService.stoptask(applyid, userid);
        renderText(res);
    }


}
