package com.hl.common.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.plugin.activerecord.Db;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint(value = "/websocket/{para}",configurator = HttpSessionConfigurator.class)
public class WebSocketController {
    private static int onlineCount = 0; //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static CopyOnWriteArraySet<WebSocketController> webSocketSet = new CopyOnWriteArraySet<WebSocketController>();
    private Session session;    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private String userid;      //用户名
    private HttpSession httpSession;    //request的session

    private static List list = new ArrayList<>();   //在线列表,记录用户名称
    private static Map routetab = new HashMap<>();  //用户名和websocket的session绑定的路由表

    @OnOpen
    public void onOpen(@PathParam(value="para") String para, Session session, EndpointConfig config) {
        System.out.println("onopen");
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1;
        this.httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
//        System.out.println(httpSession.getAttribute("access_token"));
        this.userid=para;//(String) httpSession.getAttribute("userid");    //获取当前用户
        list.add(userid);           //将用户名加入在线列表
        routetab.put(userid, session);   //将用户名和session绑定到路由表
        String message = getMessage(getOnlineCount()+"", "count",  null);
        broadcast(message);     //广播
        int logincount=Db.queryInt("select count(*) FROM logininfo WHERE stat=1 AND userid=?",userid);
        if(logincount!=0){

        }else {
            Db.update("INSERT INTO logininfo (username,userid) VALUES(?,?)",session.getId(),userid);
        }
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("onclose");
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        list.remove(userid);        //从在线列表移除这个用户
        routetab.remove(userid);
//        String message = getMessage("[" + userid +"]离开了聊天室,当前在线人数为"+getOnlineCount()+"位", "notice", list);
        String message = getMessage(getOnlineCount()+"", "count",  null);
        broadcast(message);         //广播
        Db.update("UPDATE logininfo SET stat=0,timespan= TIMESTAMPDIFF(SECOND,starttime,NOW()) WHERE userid=? AND stat=1",userid);
    }

    @OnMessage
    public void onMessage(String _message, Session session) throws IOException {
//        session.getBasicRemote().sendText(requestJson);
        JSONObject chat = JSON.parseObject(_message);
        JSONObject message = JSON.parseObject(chat.get("message").toString());
        if(message.get("to") == null || message.get("to").equals("")){      //如果to为空,则广播;如果不为空,则对指定的用户发送消息
            broadcast(_message);
        }else{
            String [] userlist = message.get("to").toString().split(",");
            singleSend(_message, (Session) routetab.get(message.get("from")));      //发送给自己,这个别忘了
            for(String user : userlist){
                if(!user.equals(message.get("from"))){
                    singleSend(_message, (Session) routetab.get(user));     //分别发送给每个指定用户
                }
            }
        }
    }
    /**
     * 广播消息
     * @param message
     */
    public void broadcast(String message){
        for(WebSocketController chat: webSocketSet){
            try {
                chat.session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * 对特定用户发送消息
     * @param message
     * @param session
     */
    public void singleSend(String message, Session session){
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 对特定用户发送消息
     * @param message
     * @param userid
     */
    public void singleSendbyUserid(String message, String userid){
        try {
            Session session= (Session) routetab.get(userid);
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 组装返回给前台的消息
     * @param message   交互信息
     * @param type      信息类型
     * @param list      在线列表
     * @return
     */
    public String getMessage(String message, String type, List list){
        JSONObject member = new JSONObject();
        member.put("message", message);
        member.put("type", type);
        member.put("list", list);
        return member.toString();
    }
    /**
     * 组装返回给前台的消息
     * @param message   交互信息
     * @param type      信息类型
     * @param taskid      在线列表
     * @return
     */
    public String gettaskMessage(String message, String type, String taskid){
        JSONObject member = new JSONObject();
        member.put("message", message);
        member.put("type", type);
        member.put("taskid", taskid);
        return member.toString();
    }
    public  int getOnlineCount() {
        return onlineCount;
    }

    public  void addOnlineCount() {
        WebSocketController.onlineCount++;
    }

    public  void subOnlineCount() {
        WebSocketController.onlineCount--;
    }
}
