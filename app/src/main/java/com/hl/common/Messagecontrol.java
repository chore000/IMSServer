package com.hl.common;

import com.alibaba.dingtalk.openapi.demo.auth.AuthHelper;
import com.alibaba.dingtalk.openapi.demo.message.ConversationMessageDelivery;
import com.alibaba.dingtalk.openapi.demo.message.LightAppMessageDelivery;
import com.alibaba.dingtalk.openapi.demo.message.MessageHelper;
//import com.alibaba.dingtalk.openapi.demo.model.Mymessageclass;
import com.dingtalk.open.client.api.model.corp.MessageBody;
import com.dingtalk.open.client.api.model.corp.MessageType;
import com.jfinal.core.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dell on 2017/2/25.
 */
public class Messagecontrol extends Controller {


    //测试应用AgentID: 46405883
    public void sendcodemessage() {
        String code = getPara("code");
        String author = getPara("author");

        System.out.println("code" + code);
        String sender = getPara("sender");
        String agentId = getPara("agentId");
        MessageBody.OABody oaBody = new MessageBody.OABody();
        MessageBody.OABody.Head head = new MessageBody.OABody.Head();
        head.setText("head.text");
        head.setBgcolor("FF00C5CD");
        oaBody.setHead(head);
        MessageBody.OABody.Body body = new MessageBody.OABody.Body();
        body.setAuthor(author);
        body.setContent("content" + new Date());
        body.setTitle("body.title");
        MessageBody.OABody.Body.Rich rich = new MessageBody.OABody.Body.Rich();
        rich.setNum("num");
        rich.setUnit("unit");
        body.setRich(rich);
        List<MessageBody.OABody.Body.Form> formList = new ArrayList<MessageBody.OABody.Body.Form>();
        MessageBody.OABody.Body.Form form = new MessageBody.OABody.Body.Form();
        form.setKey("key");
        form.setValue("value");
        formList.add(form);
        body.setForm(formList);
        oaBody.setBody(body);
        System.out.println(oaBody.toString());
            /*测试文本信息*/
        MessageBody.TextBody textBody = new MessageBody.TextBody();
        textBody.setContent("sssssssssssss");
        ConversationMessageDelivery conversationMessageDelivery = new ConversationMessageDelivery(sender, "0105453768-1837873444",
                "46405883");
        try {
            String accessToken = AuthHelper.getAccessToken();

            conversationMessageDelivery.withMessage(MessageType.TEXT, textBody);
            LightAppMessageDelivery lightAppMessageDelivery = new LightAppMessageDelivery("0105453768-1837873444", "", "46405883");
            lightAppMessageDelivery.withMessage(MessageType.OA, oaBody);
//            Mymessageclass Mymessageclass = new Mymessageclass("0105453768-1837873444", "", "46405883", code);
//            Mymessageclass.withMessage(MessageType.OA, oaBody);
//            renderText(MessageHelper.sendbycode(accessToken, Mymessageclass).toString());
        } catch (Exception e) {
            renderText(e.toString());
        }
        /*ceshi wentben xinxi*/

        /*LightAppMessageDelivery lightAppMessageDelivery = new LightAppMessageDelivery("01054537689281", "", "10851862");
        lightAppMessageDelivery.withMessage(MessageType.OA, oaBody);
        String accessToken = AuthHelper.getAccessToken();
        MessageHelper.sendbycode(accessToken, lightAppMessageDelivery, code);*/

    }

    //测试应用AgentID: 46405883
    public void sendcommsg() throws Exception {
        getRequest().setCharacterEncoding("utf-8");
        String userid = "0105453768-1837873444";//getPara("userid");
        String agentId ="46405883";// getPara("agentId");
        String messagetext = getPara("messagetext");
        // 获取access token
        String accessToken = AuthHelper.getAccessToken();
        System.out.print("成功获取access token: " + accessToken);
        MessageBody.TextBody textBody = new MessageBody.TextBody();
        textBody.setContent(messagetext);

  /*      // 发送微应用消息
        String toParties = Vars.TO_PARTY;
        LightAppMessageDelivery lightAppMessageDelivery = new LightAppMessageDelivery(userid, toParties, agentId);

        lightAppMessageDelivery.withMessage(MessageType.TEXT, textBody);
        MessageHelper.send(accessToken, lightAppMessageDelivery);*/





    }
    //修改url
    public  void sendcomoamsg() throws Exception {
        String userid = "0105453768-1837873444";//getPara("userid");
       // Db.findFirst("");
        String agentId ="46405883";// getPara("agentId");
        String author="赵海龙";
        String content="审批内容~~~~~~~~~~~~~~~~~~~~~~~~";
        String num="5583154.25";
        MessageBody.OABody oaBody = new MessageBody.OABody();
        MessageBody.OABody.Head head = new MessageBody.OABody.Head();
        head.setBgcolor("FF00C5CD");
        oaBody.setHead(head);
        oaBody.setMessage_url("http://10.3.12.75:9001");
        MessageBody.OABody.Body body = new MessageBody.OABody.Body();
        body.setAuthor(author);
        body.setContent("审批内容：" + content);
        body.setTitle("有一条新挖潜需要您审批");
        MessageBody.OABody.Body.Rich rich = new MessageBody.OABody.Body.Rich();
        rich.setNum(num);
        rich.setUnit("元");
        body.setRich(rich);
  /*      List<MessageBody.OABody.Body.Form> formList = new ArrayList<MessageBody.OABody.Body.Form>();
        MessageBody.OABody.Body.Form form = new MessageBody.OABody.Body.Form();
        form.setKey("key");
        form.setValue("value");
        formList.add(form);
        body.setForm(formList);*/
        oaBody.setBody(body);
        System.out.println(oaBody.toString());
            /*测试文本信息*/
        MessageBody.TextBody textBody = new MessageBody.TextBody();
        textBody.setContent("sssssssssssss");
        String accessToken = AuthHelper.getAccessToken();
        LightAppMessageDelivery lightAppMessageDelivery = new LightAppMessageDelivery(userid, "", agentId);
        lightAppMessageDelivery.withMessage(MessageType.OA, oaBody);
        MessageHelper.send(accessToken, lightAppMessageDelivery);
//        log("成功发送 微应用oa消息");
    }
}
