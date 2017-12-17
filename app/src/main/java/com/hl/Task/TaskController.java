package com.hl.Task;

import com.alibaba.fastjson.JSONObject;
import com.hl.argu.ArguServices;
import com.hl.bounspoint.BPServices;
import com.hl.common.websocket.WebSocketController;
import com.hl.log.LogServices;
import com.hl.model.Indexdatav;
import com.hl.model.Messagemodel;
import com.hl.model.Myerrorcode;
import com.hl.model.Taskinfo;
import com.hl.taskaplly.TaskApplyService;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

import java.sql.SQLException;

/***
 * 任务操作
 */
public class TaskController extends Controller {
    static TaskServices taskServices = new TaskServices();
    static LogServices logServices = new LogServices();
    static AssignTaskService assignTaskService = new AssignTaskService();
    static ArguServices arguServices = new ArguServices();
    static TaskApplyService taskApplyService = new TaskApplyService();
    static BPServices bpServices = new BPServices();
    Cache redis = Redis.use("userredis");
    Myerrorcode myerrorcode = new Myerrorcode();
    private WebSocketController webSocketController = new WebSocketController();

    public void index() {
        renderText("ok");
    }

    /***
     * ---新增任务，暂未使用，通过任务申请直接生成任务
     */
    public void addtask() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String spflag = getPara("sp", "");
        String taskinfo = getPara("taskinfo");
        boolean res = false;
        if ("".equals(spflag))
            res = taskServices.addTask(taskinfo);
        else
            res = taskServices.addTaskdirect(taskinfo);
//bpServices.addbp(jb.getString("userid"),"addtask",);
        if (res) {
            logServices.addlog(jb.getString("userid"), "addtask", taskinfo, "");
            renderText("success");
        } else {
            renderText("error");
        }
    }

    /***
     * 领取任务
     */
    public void assigntask() {
        String userjson = redis.get(getCookie("access_token"));
//        Object access = getCookie("user");
        JSONObject jb = JSONObject.parseObject(userjson);
        String taskid = getPara("taskid");
        String assignee = jb.getString("userid");
        String res = taskServices.chooseemployee(taskid, assignee);
        if ("{\"codemsg\":\"OK\",\"stat\":0}".equals(res)) {
            assignTaskService.addassigntask(taskid, assignee);
            logServices.addlog(jb.getString("userid"), "assigntask", "抢了一个任务", taskid);
        }
        renderText(res);
    }

    /***
     *  完成任务，后期增加事务控制
     */
    public void completetask() {
        String userjson = redis.get(getCookie("access_token"));
        final String comment = getPara("comment");
        JSONObject jb = JSONObject.parseObject(userjson);
        final String taskid = getPara("taskid");
        final String assignee = jb.getString("userid");
        boolean success = Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                boolean res = taskServices.CompleteTask(taskid, assignee);
                boolean res1 = assignTaskService.completeassigntask(taskid, assignee);
                boolean res2 = arguServices.arguaddbytaskid(comment, Integer.parseInt(taskid), assignee, "complete");
                return res && res1 && res2;
            }
        });

        if (success) {
            myerrorcode.setStat(0);
            myerrorcode.setCodemsg(success + "");
        } else {
            myerrorcode.setStat(1);
            myerrorcode.setCodemsg("res:");
        }
        renderText(JsonKit.toJson(myerrorcode));

    }

    /***
     *  检查任务，后期增加事务控制
     */
    public void checktask() {
        String userjson = redis.get(getCookie("access_token"));
        final String checkcomment = getPara("checkcomment");
        JSONObject jb = JSONObject.parseObject(userjson);
        final String taskid = getPara("taskid");
        final Record taksinfo = Db.findFirst("SELECT * FROM t_taskinfo WHERE id =?", taskid);
        final String marks = getPara("marks");
        final String stat = getPara("stat");
        final String assignee = jb.getString("userid");
        boolean success = Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                boolean res = taskServices.checkTask(taskid, assignee, stat, marks);
                boolean res1 = assignTaskService.checktask(taskid, assignee);
                boolean res2 = arguServices.arguaddbytaskid(checkcomment, Integer.parseInt(taskid), assignee, "check");
                boolean res3 = new Messagemodel().set("msgcontent", checkcomment).set("userid", assignee).set("taskid", taskid).set("senderid", taksinfo.getStr("assignee")).save();

                return res && res1 && res2 && res3;
            }
        });

        if (success) {
            String msg = webSocketController.gettaskMessage(checkcomment, "task", taskid);
            webSocketController.singleSendbyUserid(msg, taksinfo.getStr("assignee"));
            myerrorcode.setStat(0);
            myerrorcode.setCodemsg("success");
        } else {
            myerrorcode.setStat(1);
            myerrorcode.setCodemsg("res:");
        }
        renderText(JsonKit.toJson(myerrorcode));

    }

    /***
     * 放弃任务
     */
    public void giveuptask() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String taskid = getPara("taskid");
        String assignee = jb.getString("userid");
        String res = taskServices.unchooseemployee(taskid, assignee);
        assignTaskService.unassigntask(taskid, assignee);
        logServices.addlog(jb.getString("userid"), "assigntask", "放弃了一个任务", taskid);
        renderText(res);
    }


    /***
     *获取需要配合的相关任务
     */
    public void getreleasetask() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        int pagenum = getParaToInt("pagenum", 1);
        int pagesize = getParaToInt("pagesize", 10);
        renderText(taskServices.getrelesetask(assignee, pagenum, pagesize));
    }

    /***
     * 根据用户id获取本日任务
     */
    public void gettasktoday() {

        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "5");
        Page<Taskinfo> todaytask = taskServices.tasksbyassigneetoday(Integer.parseInt(pagenum), Integer.parseInt(pagesize), assignee);
        renderText(JsonKit.toJson(todaytask));
    }

    /***
     * 根据用户id获取本日检查任务
     */
    public void getchecktask() {

        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "5");
        String checktask = taskServices.getmychecktask(assignee, Integer.parseInt(pagenum), Integer.parseInt(pagesize));
        renderText(checktask);
    }

    /***
     * 查询任务详情
     */
    public void gettaskdetailbytaskid() {
        String taskid = getPara("taskid");
        renderText(taskServices.taskinfodetail(Integer.parseInt(taskid)));
    }

    /***
     * 根据申请id获取任务
     */
    public void gettaskbyapplyid() {

        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String pagenum = getPara("pagenum", "1");
        String applyid = getPara("applyid");
        String pagesize = getPara("pagesize", "10");
        Page<Taskinfo> todaytask = taskServices.alltasksbyapplyid(Integer.parseInt(pagenum), Integer.parseInt(pagesize), applyid);
        renderText(JsonKit.toJson(todaytask));
    }

    /***
     * 根据用户id获取所有任务
     */
    public void alltask() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "5");
        Page<Taskinfo> todaytask = taskServices.alltasksbyassinee(Integer.parseInt(pagenum), Integer.parseInt(pagesize), assignee);
        renderText(JsonKit.toJson(todaytask));
    }

    /***
     * 根据用户id获取所有未完成任务
     */
    public void alltaskundo() {

        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "5");
        Page<Taskinfo> todaytask = taskServices.undotasks(Integer.parseInt(pagenum), Integer.parseInt(pagesize), assignee);
        renderText(JsonKit.toJson(todaytask));
    }

    /***
     * 根据用户id获取所有完成任务
     */
    public void alltaskdo() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "5");
        Page<Taskinfo> todaytask = taskServices.tasklistbyassignee(Integer.parseInt(pagenum), Integer.parseInt(pagesize), assignee);
        renderText(JsonKit.toJson(todaytask));
    }

    /***
     * 根据用户id获取可领取任务，后期考虑增加部门限制
     */
    public void picktasks() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "5");
        String area = getPara("area", "");
        Page<Taskinfo> todaytask = taskServices.picktasks(Integer.parseInt(pagenum), Integer.parseInt(pagesize), assignee, area);

        renderText(JsonKit.toJson(todaytask));
    }

    /***
     * 根据用户id获取可领取任务，后期考虑增加部门限制
     */
    public void picktasksmy() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "5");
        String area = getPara("area", "");
        Page<Taskinfo> todaytask = taskServices.picktasksmy(Integer.parseInt(pagenum), Integer.parseInt(pagesize), assignee);

        renderText(JsonKit.toJson(todaytask));
    }

    /***
     * 获取任务概况
     */
    public void alltaskcondition() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");

        String alltaskcondition = taskServices.alltaskcondition(assignee);

        renderText(alltaskcondition);
    }

    /***
     * 根据区域获取进行中任务详情
     */
    public void taskdoingbyarea() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
//        String assignee = jb.getString("userid");
        int pagenum = getParaToInt("pagenum");
        int pagesize = getParaToInt("pagesize");
        String area = getPara("area");
        String taskdoing = taskServices.taskdoingbyarea(pagenum, pagesize, area, null);

        renderText(taskdoing);
    }


    /***
     * 获取所有用户任务日志，后期考虑增加部门限制
     */
    public void gettasklog() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String department = jb.getString("department");
        String pagenum = getPara("pagenum", "1");
        String pagesize = getPara("pagesize", "12");
        System.out.println(pagenum);
        if ("1".equals(pagenum)) {
            String tasklogs = redis.get("tasklog");
            if (tasklogs == null) {
                tasklogs = assignTaskService.gettasklog(Integer.parseInt(pagenum), Integer.parseInt(pagesize));
                redis.setex("tasklog", 60, tasklogs);
            }
            renderText(tasklogs);
        } else {
            renderText(assignTaskService.gettasklog(Integer.parseInt(pagenum), Integer.parseInt(pagesize)));
        }

    }

    /***
     * 根据用户id获取本日积分
     */
    public void getmymarktoday() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        renderText(taskServices.markbyidtoday(assignee) + "");
    }

    /***
     * 根据用户id获取所有积分
     */
    public void getmymarkall() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        renderText(taskServices.allmarkbyid(assignee) + "");
    }

    /***
     * 根据用户id获取本日完成任务
     */
    public void getcompletetoday() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        renderText(taskServices.dotaskcounttoday(assignee) + "");
    }

    /***
     * 根据用户id获取所有完成任务
     */
    public void getcompleteall() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        renderText(taskServices.dotaskcount(assignee) + "");
    }

    /***
     * 暂未维护
     */
    public void gettaskcount() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String department = jb.getString("department");
    }

    /***
     * 获取首页数据，后期考虑增加部门限制
     */
    public void indexdatav() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        Indexdatav indexdatav = new Indexdatav();
        indexdatav.setUndonum(taskServices.undotaskcount(assignee) + "");
        indexdatav.setDonum(taskServices.getrelesetaskcount(assignee) + "");  //获取相关任务数量
        indexdatav.setTasks(taskServices.picktasksnum(assignee) + "");
        indexdatav.setMarks(taskServices.markbyidtoday(assignee) + "");
        indexdatav.setSendcount(taskServices.getmychecktaskcount(assignee) + "");  //获取待审批数量
        indexdatav.setApplycount(taskApplyService.getTaskaprovecount(assignee) + "");
        renderText(JsonKit.toJson(indexdatav));
    }

    /***
     * 获取本日分数排行，后期考虑增加部门限制
     */
    public void marklisttoday() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String res = taskServices.marklisttoday();
        renderText(res);
    }

    /***
     * 获取zuo日分数排行前三，后期考虑增加部门限制
     */
    public void marklisttodaytop3() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String res = redis.get("todaytop3");
        if (res == null) {
            res = taskServices.marklisttodaytop3();
            redis.setex("todaytop3", 60 * 60 * 4, res);
        }
        renderText(res);
    }

    /***
     * 获取本月分数排行，后期考虑增加部门限制
     */
    public void marklistmonth() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String res = taskServices.marklistmonth();
        renderText(res);
    }

    /***
     * 获取合计分数排行，后期考虑增加部门限制
     */
    public void marklist() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");
        String res = taskServices.marklistall();
        renderText(res);
    }

    /***
     * 获取个人详细分数
     */
    public void getdetailmarkbyid() {
        int pagenum = getParaToInt("pagenum", 1);
        int pagesize = getParaToInt("pagesize", 5);
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = getPara("userid", jb.getString("userid"));

        String res = taskServices.markbyiddetail(pagenum, pagesize, assignee);
        renderText(res);
    }

    /***
     * 根据applyid查询任务列表
     */
    public void alltasksbyapplyid() {
        String userjson = redis.get(getCookie("access_token"));
        JSONObject jb = JSONObject.parseObject(userjson);
        String assignee = jb.getString("userid");

        String pagenum = getPara("pagenum", "1");
        String applyid = getPara("applyid");
        String pagesize = getPara("pagesize", "12");
        renderText(JsonKit.toJson(taskServices.alltasksbyapplyid(Integer.parseInt(pagenum), Integer.parseInt(pagesize), applyid)));
    }
}
