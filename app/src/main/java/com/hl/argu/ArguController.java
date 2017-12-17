package com.hl.argu;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hl.common.websocket.WebSocketController;
import com.hl.model.Messagemodel;
import com.hl.model.Myerrorcode;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import org.apache.log4j.Logger;

public class ArguController extends Controller {
    Logger logger = Logger.getLogger(this.getClass());
    private static ArguServices arguServices = new ArguServices();
    private static WebSocketController webSocketController = new WebSocketController();

    public void showargu() {
        String taskid = getPara("taskid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "5");
        String res = arguServices.argubytaskid(taskid, Integer.parseInt(pagenum), Integer.parseInt(pagesize)
        );
        renderText(res);
    }

    public void showcomment() {
        String taskid = getPara("taskid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "5");
        String res = arguServices.commentbytaskid(taskid, Integer.parseInt(pagenum), Integer.parseInt(pagesize)
        );
        renderText(res);
    }

    public void showcheckcomment() {
        String taskid = getPara("taskid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "5");
        String res = arguServices.checkcommentbytaskid(taskid, Integer.parseInt(pagenum), Integer.parseInt(pagesize)
        );
        renderText(res);
    }

    public void addargu() {
        String taskid = getPara("taskid");
        String comment = getPara("comment");
        Cache redis = Redis.use("userredis");
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String username = jb.getString("name");
        String touser = getPara("touser");
        JSONArray touserjson = JSONObject.parseArray(touser);

        boolean res = arguServices.arguaddbytaskid(comment, Integer.parseInt(taskid), assignee, "add");
        Myerrorcode myerrorcode = new Myerrorcode();
        if (res) {
            myerrorcode.setCodemsg("ok");
            myerrorcode.setStat(0);
            for (int i = 0; i < touserjson.size(); i++) {
                JSONObject touserjb = JSONObject.parseObject(touserjson.get(i) + "");
                new Messagemodel().set("msgcontent", comment).set("userid",assignee ).set("taskid", taskid).set("senderid", touserjb.getString("userid")).save();
                String message = webSocketController.gettaskMessage(username + ":" + comment, "task", taskid);
                try {
                    webSocketController.singleSendbyUserid(message, touserjb.getString("userid"));
                } catch (Exception e) {
                    logger.error("对方未在线" + e);
                }
            }

        } else {
            myerrorcode.setCodemsg("error");
            myerrorcode.setStat(-1);
        }
        renderText(JsonKit.toJson(myerrorcode));
    }

    public void delargu() {

    }


}
