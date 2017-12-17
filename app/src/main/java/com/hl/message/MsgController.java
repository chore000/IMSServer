package com.hl.message;

import com.alibaba.fastjson.JSONObject;
import com.jfinal.core.Controller;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

public class MsgController extends Controller {
    private MessageService messageService = new MessageService();
    Cache redis = Redis.use("userredis");

    public void index() {
        int pagenum = getParaToInt("pagenum", 1);
        int pagesize = getParaToInt("pagesize", 12);
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String msg = messageService.getMsgbyuserid(pagenum, pagesize, jb.getString("userid"));
        renderText(msg);
    }

    /***
     * 未启用
     */
    public void addmsg() {
        String msgjson = getPara("msgjson");
        String sendid = getPara("sendid");
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        boolean res = messageService.addMsg(msgjson, sendid);
    }

    public void readmsg() {
        String msgid = getPara("msgid");
//        String sendid = getPara("sendid");
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
//        String msg = messageService.getMsgbyuserid(jb.getString("userid"));
        boolean res = messageService.readMsg(msgid, jb.getString("userid"));
    }
}
