package com.hl.Task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hl.model.SecTask;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.sql.SQLException;
import java.util.List;

public class SecTaksServiece {
    private  SecTask sectaskdao = new SecTask();

    /***
     * 根据jsonSTR增加子任务
     * @param sectaskjson
     * @param userid
     * @return
     */
    public boolean add2ndtaskbyjson(String sectaskjson, final String userid, final String taskid) {
        final JSONArray jsonArray = JSON.parseArray(sectaskjson);
        boolean success = Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                boolean res = true;
                int num = Db.queryInt("SELECT COUNT(*) FROM t_2rdtask WHERE t_2rdtask.taskid = ? AND stat=1", taskid);
                int refnum = Db.update("UPDATE t_2rdtask SET stat=0 WHERE taskid=? AND stat=1", taskid);
                if (num != refnum) {
                    return false;
                }
                for (Object jb : jsonArray) {
                    JSONObject task = JSONObject.parseObject(jb + "");
                    boolean boss = false;
                    if (userid.equals(task.getString("userid"))) {
                        boss = true;
                    }
                    boolean res1 = new SecTask().set("taskid", taskid).set("userid", task.getString("userid")).set("mark", task.getString("mark")).set("content", task.getString("content")).set("isboss", boss).save();
                    if (!res1)
                        return false;
                    res = res && res1;
                }

                return res;
            }
        });


        return success;
    }

    /***
     * 根据taskid获取子任务列表详情
     * @param taskid
     * @return
     */
    public String gettask(int taskid) {
        List<SecTask> secTasks = sectaskdao.find("SELECT 2task.id, 2task.taskid, 2task.userid, 2task.mark, 2task.content, 2task.createtime, 2task.isboss, 2task.realmark, u.`name`, u.jobnum FROM t_2rdtask AS 2task LEFT JOIN t_ding_user AS u ON 2task.userid = u.userid WHERE 2task.taskid = ? and stat=1", taskid);
        return JsonKit.toJson(secTasks);
    }
}
