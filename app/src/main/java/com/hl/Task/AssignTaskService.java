package com.hl.Task;

import com.hl.model.AssignTask;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class AssignTaskService {
    private static final AssignTask dao = new AssignTask();

    /***
     * 任务分配
     * @param taskid
     * @param assign
     */
    public boolean addassigntask(String taskid, String assign) {
        boolean res1 = false;
        AssignTask ass = dao.findFirst("SELECT * FROM `t_assigntask` where stat=1 AND taskid=? AND assignee=?", taskid, assign);
        if (ass != null)
            res1 = ass.set("stat", 0).update();
        boolean res = new AssignTask().set("taskid", taskid).set("assignee", assign).set("action", "add").set("stat", "1").save();
        return res && res1;
    }


    /***
     * 被分配任务
     * @param taskid
     * @param assign
     */
    public boolean addassigntaskbyother(String taskid, String assign) {
//        boolean res1 = false;
       /* AssignTask ass = dao.findFirst("SELECT * FROM `t_assigntask` where stat=1 AND taskid=? AND assignee=?", taskid, assign);
        if (ass != null)
            res1 = ass.set("stat", 0).update();*/
        boolean res = new AssignTask().set("taskid", taskid).set("assignee", assign).set("action", "addbyother").set("stat", "1").save();
        return res;
    }

    /***
     * 完成时的日志
     * @param taskid
     * @param assign
     * @return
     */
    public boolean completeassigntask(String taskid, String assign) {
        boolean res1 = true;
        AssignTask ass = dao.findFirst("SELECT * FROM `t_assigntask` where stat=1 AND taskid=? AND assignee=?", taskid, assign);
        if (ass != null)
            res1 = ass.set("stat", 0).update();
        boolean res = new AssignTask().set("taskid", taskid).set("assignee", assign).set("action", "complete").set("stat", "1").save();
        return res && res1;
    }

    /***
     * 检查日志
     * @param taskid
     * @param assign
     * @return
     */
    public boolean checktask(String taskid, String assign) {
        boolean res1 = true;
        AssignTask ass = dao.findFirst("SELECT * FROM `t_assigntask` where stat=1 AND taskid=? AND assignee=?", taskid, assign);
        if (ass != null)
            res1 = ass.set("stat", 0).update();
        boolean res = new AssignTask().set("taskid", taskid).set("assignee", assign).set("action", "check").set("stat", "1").save();
        return res && res1;
    }

    /***
     * 放弃任务
     * @param taskid
     * @param assign
     * @return
     */
    public boolean unassigntask(String taskid, String assign) {

        boolean res1 = false;
        AssignTask ass = dao.findFirst("SELECT * FROM `t_assigntask` where stat=1 AND taskid=? AND assignee=?", taskid, assign);
        if (ass != null)
            res1 = ass.set("stat", 0).update();
        boolean res = new AssignTask().set("taskid", taskid).set("assignee", assign).set("action", "giveup").set("stat", "1").save();
        return res && res1;
    }

    /***
     * 返回活动日志视图
     * @param pagenum
     * @param pagesize
     * @return
     */
    public String gettasklog(int pagenum, int pagesize) {
        System.out.println("pagenum:"+ pagenum);
        Page<Record> tasklogs = Db.paginate(pagenum, pagesize, "SELECT v_tasklogs.id, v_tasklogs.taskid, v_tasklogs.assignee, v_tasklogs.action, v_tasklogs.createtime, v_tasklogs.stat, v_tasklogs.meaning, v_tasklogs.jobnum, v_tasklogs.`name`,v_tasklogs.`jobname` ", "FROM `v_tasklogs` where stat=1");
        return JsonKit.toJson(tasklogs);
    }

    /***
     * 返回活动日志视图
     * @param pagenum
     * @param pagesize
     * @return
     */
    public String gettasklogbytaskid(String taskid, int pagenum, int pagesize) {
        Page<Record> tasklogs = Db.paginate(pagenum, pagesize, "SELECT v_tasklogs.id, v_tasklogs.taskid, v_tasklogs.assignee, v_tasklogs.action, v_tasklogs.createtime, v_tasklogs.stat, v_tasklogs.meaning, v_tasklogs.jobnum, v_tasklogs.`name`,v_tasklogs.`jobname` ",
                "FROM `v_tasklogs` where  v_tasklogs.taskid=?", taskid);
        return JsonKit.toJson(tasklogs);
    }
}
