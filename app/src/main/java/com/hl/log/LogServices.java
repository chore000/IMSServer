package com.hl.log;

import com.hl.model.Mylogs;
import com.jfinal.plugin.activerecord.Page;

public class LogServices {

    private static Mylogs dao = new Mylogs().dao();

    public boolean addlog(String userid, String type, String detail, String taskid) {
        boolean res = new Mylogs().set("userid", userid).set("type", type).set("detail", detail).set("taskid", taskid).save();
        return res;
    }

    public boolean del() {
        return false;
    }

    public Page<Mylogs> allmylogs(int pagenum, int pagesize) {
        Page<Mylogs> logs = dao.paginate(pagenum, pagesize, "SELECT t_logs.id, t_logs.userid, t_logs.type, t_logs.detail, t_logs.createtime,t_logs.taskid ", "FROM t_logs ORDER BY t_logs.createtime DESC");
        return logs;
    }

    public Page<Mylogs> mylogsbytype(int pagenum, int pagesize, String type) {
        Page<Mylogs> logs = dao.paginate(pagenum, pagesize, "SELECT t_logs.id, t_logs.userid, t_logs.type, t_logs.detail, t_logs.createtime ", "FROM t_logs where type=? ORDER BY t_logs.createtime DESC", type);
        return logs;
    }
}
