package com.hl.argu;

import com.hl.model.Argumodel;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;

public class ArguServices {
    private static Argumodel dao = new Argumodel().dao();

    /***
     * 增加评论
     * @param taskid
     * @return
     */
    public boolean arguaddbytaskid(String comment, int taskid, String assignee, String type) {
        boolean res = new Argumodel().set("taskid", taskid).set("comment", comment).set("userid", assignee).set("type", type).save();
        return res;
    }

    /***
     * 根据taskid获取评论
     * @param taskid
     * @return
     */
    public String argubytaskid(String taskid, int pagenum, int pagesize) {
        Page<Argumodel> argumodel = dao.paginate(pagenum, pagesize, "SELECT  argu.*,u.`name`,u.jobnum,u.avatar ",
                "  FROM t_argu argu LEFT JOIN t_ding_user u ON u.userid=argu.userid WHERE argu.taskid =? AND argu.type='add'", taskid);
        return JsonKit.toJson(argumodel);
    }

    /***
     * 根据taskid获取工作汇报
     * @param taskid
     * @return
     */
    public String commentbytaskid(String taskid, int pagenum, int pagesize) {
        Page<Argumodel> argumodel = dao.paginate(pagenum, pagesize, "SELECT  argu.*,u.`name`,u.jobnum,u.avatar ",
                "  FROM t_argu argu LEFT JOIN t_ding_user u ON u.userid=argu.userid WHERE argu.taskid =? AND argu.type='complete'", taskid);
        return JsonKit.toJson(argumodel);
    }


    /***
     * 根据taskid获取检查汇报
     * @param taskid
     * @return
     */
    public String checkcommentbytaskid(String taskid, int pagenum, int pagesize) {
        Page<Argumodel> argumodel = dao.paginate(pagenum, pagesize, "SELECT  argu.*,u.`name`,u.jobnum,u.avatar ",
                "  FROM t_argu argu LEFT JOIN t_ding_user u ON u.userid=argu.userid WHERE argu.taskid =? AND argu.type='check'", taskid);
        return JsonKit.toJson(argumodel);
    }

    /***
     * 删除评论
     */
    public boolean delargubytaskid(int id, String assignee) {
        boolean res = dao.deleteById(id);
        return res;
    }
}
