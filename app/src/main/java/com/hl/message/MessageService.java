package com.hl.message;

import com.alibaba.fastjson.JSONObject;
import com.hl.model.Messagemodel;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;

import java.util.List;

public class MessageService {
    private Messagemodel messagemodel = new Messagemodel();

    /***
     * 新增消息
     * @return
     */
    public boolean addMsg(String msgjson, String sendid) {
        JSONObject jsonObject = JSONObject.parseObject(msgjson);

        boolean res = new Messagemodel().set("msgcontent", jsonObject.getString("msgcontent")).set("userid",
                jsonObject.getString("userid")).set("path", jsonObject.getString("path"))
                .set("taskid", jsonObject.getString("taskid")).set("senderid", sendid).save();
        return res;
    }

    /***
     * 标记消息已读
     * @return
     */
    public boolean readMsg(String msgid,String userid) {
        boolean res = messagemodel.findFirst("SELECT * FROM `t_message` WHERE t_message.id = ? and senderid=?", msgid,userid).set("stat", "0").update();
        return res;
    }

    /***
     * 根据用户id获取消息列表
     * @param userid
     * @return
     */
    public String getMsgbyuserid(int pagenum,int pagesize,String userid) {
        Page<Messagemodel> message = messagemodel.paginate(pagenum,pagesize,"SELECT msg.id, msg.stat, msg.msgcontent, msg.userid, msg.path, msg.createtime, msg.taskid, msg.senderid, u.jobnum, u.`name` " ,
                "FROM t_message AS msg LEFT JOIN t_ding_user AS u ON msg.userid = u.userid WHERE msg.senderid = ?  ORDER BY msg.createtime DESC", userid);
        return JsonKit.toJson(message);
    }
    /***
     * 根据用户id获取已读消息列表
     * @param userid
     * @return
     */
    public String getMsgbyuseridread(int pagenum,int pagesize,String userid) {
        Page<Messagemodel> message = messagemodel.paginate(pagenum,pagesize,"SELECT msg.id, msg.stat, msg.msgcontent, msg.userid, msg.path, msg.createtime, msg.taskid, msg.senderid, u.jobnum, u.`name` " ,
                "FROM t_message AS msg LEFT JOIN t_ding_user AS u ON msg.userid = u.userid WHERE msg.senderid = ? AND msg.stat = 0 ORDER BY msg.createtime DESC", userid);
        return JsonKit.toJson(message);
    }
}
