package com.hl.Task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hl.model.*;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class TaskServices {

    private static final Taskinfo dao = new Taskinfo().dao();
    private final SecTask secTaskdao = new SecTask().dao();

    /***
     * 新增任务接口 暂时未启用
     * @param task json
     * @return 是否成功
     */
    public boolean addTask(String task) {
        final JSONObject jb = JSONObject.parseObject(task);
        boolean suc = Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                boolean res = new Taskinfo().set("jobname", jb.getString("jobname")).set("jobcontent", jb.getString("jobcontent"))
                        .set("jobneed", jb.getString("jobneed"))
                        .set("mark", jb.getString("mark")).set("powerneed", jb.getString("powerneed"))
                        .set("type", jb.getString("type")).save();
                boolean res1 = new TaskidType().set("", "").save();
                return false;
            }
        });
        return suc;

    }

    /***
     * 新增任务接口(不审批)
     * @param task json
     * @return 是否成功
     */
    public boolean addTaskdirect(String task) {
        JSONObject jb = JSONObject.parseObject(task);
        boolean res = new Taskinfo().set("jobname", jb.getString("jobname")).set("jobcontent", jb.getString("jobcontent"))
                .set("jobneed", jb.getString("jobneed"))
                .set("mark", jb.getString("mark")).set("powerneed", jb.getString("powerneed"))
                .set("type", jb.getString("type")).set("stat", "0").save();
        return res;

    }

    /***
     * 获取一个人相关的任务（队员）
     * @param userid
     * @return
     */
    public String getrelesetask(String userid, int pagenum, int pagesize) {
        Page<Taskinfo> taskinfo = dao.paginate(pagenum, pagesize, "SELECT * ", "FROM t_taskinfo task WHERE stat<9 and id IN ( SELECT taskid FROM t_2rdtask WHERE userid =? AND stat = 1 )", userid);
        return JsonKit.toJson(taskinfo);
    }

    /***
     * 获取一个人相关的任务（队员）
     * @param userid
     * @return
     */
    public int getrelesetaskcount(String userid) {
        int taskcount = Db.queryInt("SELECT  count(*) FROM t_taskinfo task WHERE stat<9 and id IN ( SELECT taskid FROM t_2rdtask WHERE userid =? AND stat = 1 )", userid);
        return taskcount;
    }

    /***
     * 获取需要检查的任务（检查员）
     * @param userid
     * @param pagenum
     * @param pagesize
     * @return
     */
    public String getmychecktask(String userid, int pagenum, int pagesize) {
        Page<Taskinfo> taskinfo = dao.paginate(pagenum, pagesize, "SELECT task.*  ", "FROM t_taskinfo AS task RIGHT JOIN t_taskapply AS apply ON apply.id = task.taskapplyid WHERE apply.checkuserid = ? AND task.stat = 9", userid);
        return JsonKit.toJson(taskinfo);
    }

    /***
     * 获取需要检查的任务数量（检查员）
     * @param userid
     * @return
     */
    public int getmychecktaskcount(String userid) {
        int taskcount = Db.queryInt("SELECT  count(*) FROM t_taskinfo AS task RIGHT JOIN t_taskapply AS apply ON apply.id = task.taskapplyid WHERE apply.checkuserid = ? AND task.stat = 9", userid);
        return taskcount;
    }
    /* *//***
     * 使用积分新增任务接口
     * @param task json
     * @return 是否成功
     *//*
    public boolean addTaskusebp(String task,String userid) {
        JSONObject jb = JSONObject.parseObject(task);
        boolean res = new Taskinfo().set("jobname", jb.getString("jobname")).set("jobcontent", jb.getString("jobcontent"))
                .set("jobneed", jb.getString("jobneed"))
                .set("mark", jb.getString("mark")).set("powerneed", jb.getString("powerneed"))
                .set("type", jb.getString("type")).save();

        return res;

    }*/

    /***
     * 更新任务
     * @param task json
     * @return 是否成功
     */
    public boolean updateTask(String task, String userid) {
        JSONObject jb = JSONObject.parseObject(task);

        boolean res = dao.findById(task).set("jobname", jb.getString("jobname")).set("jobcontent", jb.getString("jobcontent"))
                .set("jobneed", jb.getString("jobneed"))
                .set("mark", jb.getString("mark")).set("powerneed", jb.getString("powerneed"))
                .set("type", jb.getString("type")).update();
        return res;
    }

    /***
     * 检查任务
     * @param taskid 任务ID
     * @param assignee
     * @param stat
     * @return 是否成功
     */
    public boolean checkTask(final String taskid, String assignee, String stat, final String realmarks) {
        final Taskinfo taskinfo = dao.findFirst("SELECT * FROM t_taskinfo WHERE t_taskinfo.id=? AND t_taskinfo.stat=9 ", taskid);
        final SecTask secTask = secTaskdao.findFirst("SELECT * FROM `t_2rdtask` WHERE t_2rdtask.isboss = 1 AND t_2rdtask.stat = 1 AND t_2rdtask.taskid=?", taskid);
        final int i = Db.queryInt("SELECT COUNT(1) FROM t_bp WHERE t_bp.type = 'assigntask' AND t_bp.stat = 1 AND t_bp.taskid =?", taskid);
        boolean res = false;
        final Date now = Db.queryDate("select now()");
        if (taskinfo != null) {
            if ("1".equals(stat)) {

                boolean updatemarks = Db.tx(new IAtom() {
                    @Override
                    public boolean run() throws SQLException {
                        int flag = 1;
                        JSONArray jas = JSONObject.parseArray(realmarks);
                        for (Object ja : jas) {
                            JSONObject jb = JSONObject.parseObject(ja + "");
                            int i = Db.update("UPDATE t_2rdtask SET realmark=? WHERE id=?", jb.getString("mark"), jb.getString("id"));
                            flag *= i;
                        }
                        return flag > 0;
                    }
                });
                if (!updatemarks)
                    return false;
                boolean success = Db.tx(new IAtom() {
                    @Override
                    public boolean run() throws SQLException {
                        boolean res1 = taskinfo.set("stat", "10").set("checktime", now).update();
                        List<Record> records = Db.find("SELECT * FROM `t_2rdtask` WHERE t_2rdtask.taskid = ?", taskid);
                        boolean res2 = true;
                        for (Record record : records) {
                            boolean ss = new BpModel().set("type", "completetask").set("taskid", taskid).set("userid", record.getStr("userid")).set("stat", "1").set("marks", record.getDouble("realmark")).save();
                            res2 = res2 && ss;
                        }
                        //任务积分返还
                        boolean res3 = true;
                        if ("1".equals(i + ""))
                            res3 = new BpModel().set("type", "ex").set("taskid", taskid).set("userid", secTask.getStr("userid")).set("stat", "1").set("marks", "5").save();
                        return res1 && res2 && res3;
                    }
                });
                return success;
            } else {
                boolean success = Db.tx(new IAtom() {
                    @Override
                    public boolean run() throws SQLException {
                        boolean res1 = taskinfo.set("stat", "11").set("checktime", now).update();

                        return res1;
                    }
                });
                return success;
            }
        }
        return res;
    }

    /***
     * 完成任务成功
     * @param taskid 任务ID
     * @param assignee
     * @return 是否成功
     */
    public boolean CompleteTask(String taskid, String assignee) {
        Taskinfo taskinfo = dao.findFirst("SELECT * FROM t_taskinfo WHERE t_taskinfo.id=? AND t_taskinfo.stat<>9  AND t_taskinfo.assignee=?", taskid, assignee);
        Date now = Db.queryDate("select now()");
        boolean res1 = taskinfo.set("stat", "9").set("completetime", now).update();
        /*boolean res = false;
        if (taskinfo != null) {

            boolean success = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {

//                    List<Record> records = Db.find("SELECT * FROM `t_2rdtask` WHERE t_2rdtask.taskid = ?", taskid);
//                    boolean res2 = true;
//                    for (Record record : records) {
//                        boolean ss = new BpModel().set("type", "completetask").set("taskid", taskid).set("userid", record.getStr("userid")).set("stat", "1").set("marks", record.getDouble("mark")).save();
//                        res2 = res2 && ss;
//                    }
//                    boolean res3 = new BpModel().set("type", "ex").set("taskid", taskid).set("userid", assignee).set("stat", "1").set("marks", "1").save();
                    return res1;
                }
            });
            return success;
        }*/
        return res1;
    }

    /***
     * 完成任务失败
     * @param taskid 任务ID
     * @param assignee
     * @return 是否成功
     */
    public boolean CompleteTaskfail(String taskid, String assignee) {
        Taskinfo taskinfo = dao.findFirst("SELECT * FROM t_taskinfo WHERE t_taskinfo.id=? AND t_taskinfo.stat<>7 AND t_taskinfo.assignee=?", taskid, assignee);
        boolean res = false;
        if (taskinfo != null) {
            Date now = Db.queryDate("select now()");
            res = taskinfo.set("stat", "7").set("completetime", now).update();


        }
        return res;
    }

    /***
     * 删除任务
     * @param taskid 任务ID
     * @return 是否成功
     */
    public boolean delTask(int taskid) {
        boolean res = dao.deleteById(taskid);
        return res;
    }

    /***
     * 根据任务ID查询任务详请
     * @param taskid
     * @return 单任务详情
     */
    public String taskinfodetail(int taskid) {
        Taskinfo task = dao.findFirst(" SELECT task.id, task.jobname, task.jobcontent, task.deadline, task.mark, task.creattime, task.completetime, task.powerneed, task.type, task.stat, task.assignee, task.tasklvl, task.taskapplyid, apply.createuserid, apply.createtime, apply.confuserid, apply.conftime, apply.stat AS applystat, apply.checkuserid, apply.checktime, apply.admin, apply.receivetime, u1.jobnum AS cjobnum, u1.`name` AS cname FROM t_taskinfo AS task LEFT JOIN t_taskapply AS apply ON task.taskapplyid = apply.id LEFT JOIN t_ding_user AS u1 ON apply.createuserid = u1.userid" +
                " WHERE task.id = ?", taskid);
        return JsonKit.toJson(task);
    }

    /***
     * 根据接单人ID查询所有接收的列表
     * @param assi 接单人ID
     * @return 任务列表
     */
    public Page<Taskinfo> alltasksbyassinee(int pagenum, int pagesize, String assi) {
        Page<Taskinfo> tasks = dao.paginate(pagenum, pagesize, "SELECT * ", "FROM t_taskinfo WHERE t_taskinfo.assignee =?", assi);
        return tasks;
    }

    /***
     * 根据applyID查询所有接收的列表
     * @param applyid 接单人ID
     * @return 任务列表
     */
    public Page<Taskinfo> alltasksbyapplyid(int pagenum, int pagesize, String applyid) {
        Page<Taskinfo> tasks = dao.paginate(pagenum, pagesize, "SELECT * ", "FROM v_taskinfo WHERE taskapplyid =?", applyid);
        return tasks;
    }

    /***
     * 根据接单人ID查询当天的任务清单
     * @param assi 接单人ID
     * @return 任务列表
     */
    public Page<Taskinfo> tasksbyassigneetoday(int pagenum, int pagesize, String assi) {
        Page<Taskinfo> tasks = dao.paginate(pagenum, pagesize, "SELECT *", " FROM t_taskinfo WHERE t_taskinfo.assignee =?  and  to_days(creattime) = to_days(now())", assi);
        return tasks;
    }

    /***
     * 根据接单人ID查询已完成的任务清单
     * @param pagenum
     * @param pagesize
     * @param assi
     * @return 任务清单
     */
    public Page<Taskinfo> tasklistbyassignee(int pagenum, int pagesize, String assi) {
//        Page<Taskinfo> tasks = dao.paginate(pagenum, pagesize, "SELECT *", " FROM t_taskinfo WHERE t_taskinfo.assignee =?  and  stat>=9 order by completetime desc", assi);
        Page<Taskinfo> tasks = dao.paginate(pagenum, pagesize, "SELECT *", " FROM t_taskinfo WHERE id IN ( SELECT taskid FROM t_2rdtask WHERE userid =? AND stat = 1 ) and  stat>=9 order by completetime desc", assi);
        return tasks;
    }


    /***
     * 根据接单人ID查询所有的未完成任务
     * @param assi 接单人ID
     * @return 任务列表
     */
    public Page<Taskinfo> undotasks(int pagenum, int pagesize, String assi) {
        Page<Taskinfo> tasks = dao.paginate(pagenum, pagesize, "SELECT * ", "FROM t_taskinfo WHERE t_taskinfo.assignee =? and stat=1", assi);
        return tasks;
    }

    /***
     * 根据接单人ID查询所有的任务
     * @param assi 接单人ID
     * @return 任务列表
     */
    public Page<Taskinfo> picktasks(int pagenum, int pagesize, String assi, String area) {

        Page<Taskinfo> tasks = null;
        if ("".equals(area)) {
            tasks = dao.paginate(pagenum, pagesize, "SELECT * ", "FROM t_taskinfo WHERE  IFNULL(t_taskinfo.assignee,'')='' and stat=0 and IFNULL(publishtime,NOW())<=NOW()");

        } else {

            tasks = dao.paginate(pagenum, pagesize, "SELECT * ", "FROM t_taskinfo WHERE  IFNULL(t_taskinfo.assignee,'')='' and stat=0 and IFNULL(publishtime,NOW())<=NOW() and area=?", area);
        }
        return tasks;
    }

    /***
     * 根据接单人ID查询所有的能力内可抢任务
     * @param assi 接单人ID
     * @return 任务列表
     */
    public Page<Taskinfo> picktasksmy(int pagenum, int pagesize, String assi) {

        Page<Taskinfo> tasks = null;
        Record my = Db.findFirst("SELECT * FROM `t_usertype` WHERE userid=?;", assi);

        tasks = dao.paginate(pagenum, pagesize, "SELECT task.* ", "FROM t_taskinfo AS task LEFT JOIN t_taskidtype AS type ON task.id = type.taskid WHERE IFNULL(task.assignee, '') = '' AND task.stat = 0 AND IFNULL(publishtime, NOW()) <= NOW() AND type.type = ? AND task.tasklvl >= ?", my.getStr("type"), my.getStr("level"));

        return tasks;
    }

    /***
     * 根据接单人ID查询所有的可抢任务数量
     * @param assi 接单人ID
     * @return 任务数量
     */
    public int picktasksnum(String assi) {
        int count = Db.queryInt("SELECT count(1) FROM t_taskinfo WHERE IFNULL(t_taskinfo.assignee,'')='' and stat=0 and IFNULL(publishtime,NOW())<=NOW() ");
        return count;
    }

    /***
     * 根据接单人ID查询任务概况
     * @param assi 接单人ID
     * @return 任务数量
     */
    public String alltaskcondition(String assi) {
        List<Record> taskcound = Db.find("SELECT * FROM(SELECT area,SUM(case WHEN stat=0 and IFNULL(t_taskinfo.assignee,'')='' THEN 1 END ) AS todo ,SUM(case WHEN stat>0 THEN 1 END ) AS doing,COUNT(1) num FROM t_taskinfo WHERE stat>=0 AND stat<9 and IFNULL(publishtime,NOW())<=NOW() GROUP BY area) as t LEFT JOIN t_area area ON area.id=t.area");
        return JsonKit.toJson(taskcound);
    }

    /***
     * 根据接单人ID,区域查询运行中任务
     * @param area 区域
     * @param assi 接单人ID
     * @return 任务数量
     */
    public String taskdoingbyarea(int pagenum, int pagesize, String area, String assi) {
//        List<Record> taskcound = Db.find("");
//        return JsonKit.toJson(taskcound);
        Page<Taskinfo> tasks = dao.paginate(pagenum, pagesize, "SELECT task.id, task.jobname, task.jobcontent, task.deadline, task.mark, task.creattime, task.completetime, task.powerneed, task.type, task.stat, " +
                        "task.assignee, task.tasklvl, task.taskapplyid, task.checktime, task.area, task.publishtime, task.workspace, `user`.jobnum, `user`.`name` ",
                "FROM t_taskinfo AS task LEFT JOIN t_ding_user AS `user` ON task.assignee = `user`.userid WHERE task.stat > 0 AND task.stat < 9 AND task.area = ?", area);
        return JsonKit.toJson(tasks);

    }


    /***
     * 发布任务
     * @param taskid
     * @return 是否成功
     */
    public boolean activeTask(int taskid) {
        boolean res = dao.findById(taskid).set("stat", "1").update();
        return res;
    }

    /***
     * 任务下线
     * @param taskid
     * @return 是否成功
     */
    public boolean unactiveTask(int taskid) {
        boolean res = dao.findById(taskid).set("stat", "0").update();
        return res;
    }


    /***
     * 领取任务
     * @param taskid
     * @param assignee
     * @return json 返回分配结果
     */
    public String chooseemployee(final String taskid, final String assignee) {

        Myerrorcode ec = new Myerrorcode();
        Record person = Db.findFirstByCache("sampleCache2", "assignee" + assignee, "SELECT t_usertype.id, t_usertype.userid, t_usertype.type,t_usertype.`level` FROM t_usertype WHERE t_usertype.userid = ?", assignee);
        if (person == null) {
            ec.setStat(-1);
            ec.setCodemsg("未维护职业信息");
        } else {
            String persontype = person.get("type") + "";
            Taskinfo tasktype = dao.findFirstByCache("sampleCache1", "taskid" + taskid, "SELECT * FROM t_taskinfo WHERE t_taskinfo.id =?", taskid);
            String type = tasktype.getStr("type");
            int reqlvl = tasktype.getInt("tasklvl");
            int lvl = Integer.parseInt(person.getStr("level"));
            String[] types = type.substring(1, type.length() - 1).split(",");
            int i = 0;
            for (String onetype : types) {
                if (onetype.equals(persontype)) {
                    i += 1;
                }
            }
            if (i == 0) {
                ec.setStat(-1);
                ec.setCodemsg("职业属性不符合");
            } else if (reqlvl < lvl) {
                ec.setStat(-1);
                ec.setCodemsg("等级要求不符合");
            } else {
                final Taskinfo task = dao.findFirst("SELECT * FROM t_taskinfo WHERE t_taskinfo.id =?", taskid);
                if ("0".equals(task.getStr("stat"))) {

                    boolean succ = Db.tx(new IAtom() {
                        @Override
                        public boolean run() throws SQLException {
                            //整改为数据库获取扣分比例
                            boolean add = new BpModel().set("type", "assigntask").set("taskid", taskid).set("userid", assignee).set("stat", "1").set("marks", "-5").save();
                            boolean res = task.set("stat", 1).set("assignee", assignee).update();
                            return res && add;
                        }
                    });

                    if (succ) {
                        ec.setStat(0);
                        ec.setCodemsg("OK");
                    } else {
                        ec.setStat(1);
                        ec.setCodemsg("error");
                    }
                } else {
                    ec.setStat(-1);
                    ec.setCodemsg("此任务已经被领取");
                }
            }
        }
        return JSONObject.toJSONString(ec);
    }

    /***
     * 放弃任务
     * @param taskid
     * @param assignee
     * @return code码
     */
    public String unchooseemployee(final String taskid, String assignee) {

        Myerrorcode ec = new Myerrorcode();
        List<Taskinfo> task = dao.find("SELECT * FROM t_taskinfo WHERE t_taskinfo.id =? AND t_taskinfo.stat<>0  and assignee=?", taskid, assignee);
        final List<SecTask> secTasks = secTaskdao.find("SELECT * FROM t_2rdtask WHERE taskid=? AND stat=1", taskid);
        if (!"0".equals(task.size())) {
            boolean success = Db.tx(new IAtom() {
                @Override
                public boolean run() throws SQLException {
                    boolean res = dao.findById(taskid).set("stat", "0").set("assignee", null).update();
                    int res1 = Db.update("UPDATE t_2rdtask SET stat=0 WHERE taskid=? AND stat=1", taskid);
                    if (secTasks.size() > 0)
                        return res && res1 > 0;
                    else
                        return res;
                }
            });

            if (success) {
                ec.setStat(0);
                ec.setCodemsg("OK");
            } else {
                ec.setStat(1);
                ec.setCodemsg("error");
            }
        } else {
            ec.setStat(-1);
            ec.setCodemsg("无此任务");
        }
        return JSONObject.toJSONString(ec);
    }

    /***
     * 未完成数量
     * @param assi
     * @return
     */
    public int undotaskcount(String assi) {
        int num = Db.queryInt("SELECT count(1) FROM t_taskinfo WHERE  assignee=? and stat=1", assi);
        return num;
    }

    /***
     * 今天完成数量
     * @param assi
     * @return
     */
    public int dotaskcounttoday(String assi) {
        int num = Db.queryInt("SELECT count(1) FROM t_taskinfo WHERE  id IN ( SELECT taskid FROM t_2rdtask WHERE userid =? AND stat = 1 ) and stat=10 and  to_days(completetime) = to_days(now())", assi);
        return num;
    }

    /***
     * 总计完成数量
     * @param assi
     * @return
     */
    public int dotaskcount(String assi) {
        int num = Db.queryInt("SELECT count(1) FROM t_taskinfo WHERE  id IN ( SELECT taskid FROM t_2rdtask WHERE userid =? AND stat = 1 ) and stat=10 ", assi);
        return num;
    }

    /***
     * 某人今天分数
     * @param assi
     * @return
     */
    public double markbyidtoday(String assi) {
//        int num = Db.queryInt("SELECT IFNULL(sum(mark),0) FROM t_taskinfo WHERE t_taskinfo.assignee =? and stat=9  and  to_days(completetime) = to_days(now())", assi);
        double num = Db.queryDouble("SELECT IFNULL(SUM(marks),0) FROM `t_bp` WHERE  to_days(createtime) = to_days(now()) and stat=1 and userid=?;", assi);
        return num;
    }

    /***
     * 某人分数
     * @param assi
     * @return
     */
    public double markbyid(String assi) {
//        int num = Db.queryInt("SELECT IFNULL(sum(mark),0) FROM t_taskinfo WHERE t_taskinfo.assignee =? and stat=9  and  to_days(completetime) = to_days(now())", assi);
        double num = Db.queryDouble("SELECT IFNULL(SUM(marks),0) FROM `t_bp` WHERE stat=1 and userid=?;", assi);
        return num;
    }

    /***
     * 某人详细分数
     * @param assi
     * @return
     */
    public String markbyiddetail(int pagenum, int pagesize, String assi) {
//        int num = Db.queryInt("SELECT IFNULL(sum(mark),0) FROM t_taskinfo WHERE t_taskinfo.assignee =? and stat=9  and  to_days(completetime) = to_days(now())", assi);
        Page<Record> marks = Db.paginate(pagenum, pagesize, "SELECT bp.id, bp.type, bp.createtime, bp.stat, bp.userid, bp.taskid, bp.remark, bp.marks, action.meaning, info.jobname",
                " FROM t_bp AS bp LEFT JOIN t_actioncomment AS action ON action.action = bp.type LEFT JOIN t_taskinfo AS info ON info.id = bp.taskid WHERE bp.userid=? and bp.stat=1 ORDER BY bp.createtime DESC", assi);
        return JsonKit.toJson(marks);
    }

    /***
     * 某人总分数
     * @param assi
     * @return
     */
    public int allmarkbyid(String assi) {
        int num = Db.queryInt("SELECT  IFNULL(sum(marks),0) FROM `t_bp` WHERE userid=? and stat=1  ", assi);
        return num;
    }

    /***
     * 今日分数列表
     * @return
     */
    public String marklisttoday() {
        List<Record> marklist = Db.find("select t.*, u.*, type.type  FROM ( SELECT userid,IFNULL(SUM(marks),0) mark,SUM( case WHEN type='completetask' THEN 1 ELSE 0 END  ) num   FROM t_bp WHERE  (TO_DAYS(NOW())- TO_DAYS(createtime)) =1  and stat=1 GROUP BY userid ) AS t LEFT JOIN t_ding_user u ON u.userid = t.userid  LEFT JOIN t_usertype type ON type.userid = t.userid ORDER BY t.mark DESC");
        return JsonKit.toJson(marklist);
    }

    /***
     * 今日分数列表top3
     * @return
     */
    public String marklisttodaytop3() {
        List<Record> marklist = Db.find("select t.*, u.*, type.type  FROM ( SELECT userid,IFNULL(SUM(marks),0) mark,SUM( case WHEN type='completetask' THEN 1 ELSE 0 END  ) num  FROM t_bp WHERE stat=1 and (TO_DAYS(NOW())- TO_DAYS(createtime)) =1 GROUP BY userid ) AS t LEFT JOIN t_ding_user u ON u.userid = t.userid LEFT JOIN t_usertype type ON type.userid = t.userid  WHERE t.mark>0 ORDER BY t.mark DESC LIMIT 3");
        return JsonKit.toJson(marklist);
    }

    /***
     * 本月分数列表
     * @return
     */
    public String marklistmonth() {
        List<Record> marklist = Db.find("SELECT t.*, u.*, type.type FROM ( SELECT userid, IFNULL(SUM(marks), 0) mark, SUM( CASE WHEN type = 'completetask' THEN 1 ELSE 0 END ) num FROM t_bp WHERE stat = 1 AND DATE_FORMAT(createtime, '%Y%m') = DATE_FORMAT(CURDATE(), '%Y%m') GROUP BY userid ) AS t LEFT JOIN t_ding_user u ON u.userid = t.userid LEFT JOIN t_usertype type ON type.userid = t.userid ORDER BY t.mark DESC");
        return JsonKit.toJson(marklist);
    }

    /***
     * 总分数列表
     * @return
     */
    public String marklistall() {
        List<Record> marklist = Db.find("SELECT t.*, u.*, type.type FROM ( SELECT userid, IFNULL(SUM(marks), 0) mark, SUM( CASE WHEN type = 'completetask' THEN 1 ELSE 0 END ) num FROM t_bp WHERE stat = 1 GROUP BY userid ) AS t LEFT JOIN t_ding_user u ON u.userid = t.userid LEFT JOIN t_usertype type ON type.userid = t.userid ORDER BY t.mark DESC");
        return JsonKit.toJson(marklist);
    }


}
