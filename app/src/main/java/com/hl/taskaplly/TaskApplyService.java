package com.hl.taskaplly;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hl.Task.AssignTaskService;
import com.hl.model.BpModel;
import com.hl.model.TaskApplymodel;
import com.hl.model.TaskidType;
import com.hl.model.Taskinfo;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.sql.SQLException;
import java.util.List;

/***
 * 任务申请记录
 */
public class TaskApplyService {
    private static TaskApplymodel dao = new TaskApplymodel().dao();


    /***
     * 获取某人申请的任务列表
     * @param userid
     * @return
     */
    public String getTaskapplymysendlist(String pagenum, String pagesize, String userid) {
        Page<TaskApplymodel> applyList = dao.paginate(Integer.parseInt(pagenum), Integer.parseInt(pagesize), "SELECT apply.id, apply.taskapply, apply.createuserid, apply.createtime, apply.confuserid, apply.conftime, apply.stat, senduser.`name` AS sendusername," +
                        " senduser.jobnum AS senduserjobnum, confuser.`name` AS confusername, confuser.jobnum AS confuserjobnum "
                , "FROM t_taskapply AS apply LEFT JOIN t_ding_user AS senduser ON apply.createuserid = senduser.userid LEFT JOIN t_ding_user AS confuser ON apply.confuserid = confuser.userid" +
                        " WHERE 1=1 AND  apply.createuserid=? order by apply.id desc", userid);
        return JsonKit.toJson(applyList);
    }

    /***
     * 获取某人的待审批列表
     * @param userid
     * @return
     */
    public String getTaskapplytodolist(String pagenum, String pagesize, String userid) {
        Page<TaskApplymodel> applyList = dao.paginate(Integer.parseInt(pagenum), Integer.parseInt(pagesize), "SELECT apply.id, apply.taskapply, apply.createuserid, apply.createtime, apply.confuserid, apply.conftime, apply.stat, senduser.`name` AS sendusername, " +
                        "senduser.jobnum AS senduserjobnum, confuser.`name` AS confusername, confuser.jobnum AS confuserjobnum",
                " FROM t_taskapply AS apply LEFT JOIN t_ding_user AS senduser ON apply.createuserid = senduser.userid LEFT JOIN t_ding_user AS confuser ON apply.confuserid = confuser.userid " +
                        "WHERE apply.stat <0 AND apply.confuserid =?", userid);
        return JsonKit.toJson(applyList);
    }

    /***
     * 获取某人的待审批数量
     * @param userid
     * @return
     */
    public String getTaskaprovecount(String userid) {
        int applyListcount = Db.queryInt("SELECT COUNT(*) FROM t_taskapply WHERE t_taskapply.confuserid = ? AND t_taskapply.stat <0", userid);
        return applyListcount + "";
    }

    /***
     * 获取某人的当天发出的任务数量
     * @param userid
     * @return
     */
    public String getmyapplycount(String userid) {
        int applyListcount = Db.queryInt("SELECT COUNT(*) FROM t_taskapply WHERE t_taskapply.createuserid = ? AND to_days(t_taskapply.createtime) = to_days(now())", userid);
        return applyListcount + "";
    }

    /***
     * 新增任务,流程开始
     * @param taskaplly
     * @return
     */
    public boolean addTaskapply(final String taskaplly, final String cuserid, final String stat) {
        final JSONObject jb = JSONObject.parseObject(taskaplly);
        final String confuser = jb.getString("admin");


        boolean succeed = Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                //申请信息
                boolean res = new TaskApplymodel().set("taskapply", taskaplly).set("createuserid", cuserid).set("confuserid", confuser).set("stat", stat).set("checkuserid", jb.getString("checker")).set("admin", jb.getString("admin")).save();
                boolean res1 = false, res2 = false;
                TaskApplymodel taskid = dao.findFirst("SELECT LAST_INSERT_ID() as id");
                int size = jb.getIntValue("taskcount");
                for (int i = 0; i < size; i++) {
                    //任务详细信息
                    res1 = new Taskinfo().set("jobname", jb.getString("taskname")).set("jobcontent", jb.getString("taskcontent"))
                            .set("deadline", jb.getString("reqtime"))
                            .set("mark", jb.getString("taskmark")).set("tasklvl", jb.getString("tasklvl")).set("type", jb.getString("tasktypelist"))
                            .set("assignee", jb.getString("assignee")).set("taskapplyid", taskid.get("id") + "").set("area", jb.getIntValue("taskarea")).set("publishtime", jb.getString("publishtime")).save();
                    Record rec = Db.findFirst("SELECT LAST_INSERT_ID() as id");

                    JSONArray jaa = JSONObject.parseArray(jb.getString("tasktypelist"));
                    boolean restype = true;
                    for (Object type : jaa) {
                        System.out.println(type + "");
                        boolean addstat = new TaskidType().set("taskid", rec.getStr("id")).set("type", type + "").save();
                        restype = restype && addstat;
                    }
                    //积分信息

                    res2 = new BpModel().set("type", "addtask").set("taskid", rec.getStr("id")).set("userid", cuserid).set("stat", "1").set("marks", "-" + jb.getString("taskmark")).save();
                    res = res1 & res & res2 && restype;
                }
                return res;
            }
        });
        return succeed;

    }

    /***
     * 根据申请ID更新任务
     * @param applyid
     * @return
     */
    public String updateTaskapply(String applyid) {
        List<TaskApplymodel> applyList = dao.find("", applyid);
        return JsonKit.toJson(applyList);
    }

    /***
     * 审批任务：通过
     * @param applyid
     * @param userid
     * @return
     */
    public boolean aprovetask(final String applyid, String userid) {
        final TaskApplymodel taskApplymodel = dao.findFirst("SELECT * FROM `t_taskapply` WHERE t_taskapply.id = ?", applyid);
        boolean succeed = false;
        //处理是否制定人员，不指定
        if (taskApplymodel.getInt("stat") == -1) {
            succeed = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {


                    boolean res = taskApplymodel.set("stat", "0").update();
                    int res1 = Db.update("UPDATE `t_taskinfo` SET stat=0 WHERE taskapplyid=?", applyid);
                    return res && res1 > 0;
                }
            });
        }
        //指定人员
        else if (taskApplymodel.getInt("stat") == -2) {
            succeed = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    Record record = Db.findFirst("SELECT * FROM `t_taskinfo` WHERE t_taskinfo.taskapplyid = ?", applyid);
                    AssignTaskService assignTaskService = new AssignTaskService();
                    assignTaskService.addassigntaskbyother(record.getStr("id"), record.getStr("assignee"));
                    boolean res = taskApplymodel.set("stat", "0").update();
                    int res1 = Db.update("UPDATE `t_taskinfo` SET stat=1 WHERE taskapplyid=?", applyid);
                    return res && res1 > 0;
                }
            });
        }

        return succeed;
    }

    /***
     * 审批任务：拒绝
     * @param applyid
     * @param userid
     * @return
     */
    public boolean rejecttask(final String applyid, String userid) {
        boolean succeed = Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                TaskApplymodel taskApplymodel = dao.findFirst("SELECT * FROM `t_taskapply` WHERE t_taskapply.id = ? and stat<0", applyid);
                if (taskApplymodel == null)
                    return false;
                boolean res = taskApplymodel.set("stat", "3").update();
                List<Record> taskinfo = Db.find("SELECT t_taskinfo.id, t_taskinfo.jobname, t_taskinfo.jobcontent, t_taskinfo.deadline, t_taskinfo.mark, t_taskinfo.creattime, t_taskinfo.completetime, t_taskinfo.powerneed, t_taskinfo.type, t_taskinfo.stat, t_taskinfo.assignee, t_taskinfo.tasklvl, t_taskinfo.taskapplyid FROM t_taskinfo WHERE t_taskinfo.taskapplyid = ?", applyid);
                boolean resbp = true;
                for (int i = 0; i < taskinfo.size(); i++) {
                    boolean reso = new BpModel().set("type", "rejtask").set("taskid", taskinfo.get(i).getStr("id")).set("userid", taskApplymodel.getStr("createuserid")).set("stat", "1").set("marks", taskinfo.get(i).getStr("mark")).save();
                    resbp = resbp && reso;
                }
                int res1 = Db.update("UPDATE `t_taskinfo` SET stat=-3 WHERE taskapplyid=?", applyid);
                //增加分数返回
//                = Db.update("", "");
                return res && res1 > 0 && resbp;
            }
        });
        return succeed;
    }

    /***
     * 审批任务：终止
     * @param applyid
     * @param userid
     * @return
     */
    public String stoptask(final String applyid, String userid) {
        int count = Db.queryInt("SELECT COUNT(*) FROM t_taskinfo WHERE  taskapplyid=? AND stat>0", applyid);
        if (count > 0)
            return "任务已经被领取";
        else {
            boolean succeed = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    TaskApplymodel taskApplymodel = dao.findFirst("SELECT * FROM `t_taskapply` WHERE t_taskapply.id = ? and stat<=0", applyid);
                    if (taskApplymodel == null)
                        return false;
                    boolean res = taskApplymodel.set("stat", "2").update();
                    List<Record> taskinfo = Db.find("SELECT t_taskinfo.id, t_taskinfo.jobname, t_taskinfo.jobcontent, t_taskinfo.deadline, t_taskinfo.mark, t_taskinfo.creattime, t_taskinfo.completetime, t_taskinfo.powerneed, t_taskinfo.type, t_taskinfo.stat, t_taskinfo.assignee, t_taskinfo.tasklvl, t_taskinfo.taskapplyid FROM t_taskinfo WHERE t_taskinfo.taskapplyid = ?", applyid);
                    boolean resbp = true;
                    for (int i = 0; i < taskinfo.size(); i++) {
                        boolean reso = new BpModel().set("type", "stoptask").set("taskid", taskinfo.get(i).getStr("id")).set("userid", taskApplymodel.getStr("createuserid")).set("stat", "1").set("marks", taskinfo.get(i).getStr("mark")).save();
                        resbp = resbp && reso;
                    }
                    int res1 = Db.update("UPDATE `t_taskinfo` SET stat=-3 WHERE taskapplyid=?", applyid);
                    //增加分数返回
//                = Db.update("", "");
                    return res && resbp && res1 > 0;
                }
            });
            return succeed + "";
        }
    }
}
