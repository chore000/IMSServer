package com.hl.Task;

import com.hl.model.TaskModel;
import com.hl.model.WorkplaceModel;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import java.util.List;

public class TasktypeServices {
//  private static   Areamodel areamodel=new Areamodel();

    /***
     * 考虑后期增加人员权限
     * @param access_token
     */
    public String gettasktype(String access_token) {
        List<Record> types = Db.find("SELECT t_tasktype.id AS `key`, t_tasktype.typename AS `value` FROM t_tasktype");//"sampleCache1", "tasktype",
        return JsonKit.toJson(types);
    }

    /***
     * 获取区域列表
     * @return
     */
    public String gettaskArea() {
        List<Record> areas = Db.find("SELECT t_area.id AS `key`, t_area.`name` AS `value` FROM `t_area`");//"sampleCache1", "tasktype",
        return JsonKit.toJson(areas);
    }
    /***
     * 获取级别名称
     * @return
     */
    public String getlvlname() {
        List<Record> areas = Db.find("SELECT t_userlvlname.id AS `key`, t_userlvlname.`name` AS `value`, t_userlvlname.updatetime FROM `t_userlvlname`");//"sampleCache1", "tasktype",
        return JsonKit.toJson(areas);
    }
    /***
     * 获取任务模板
     */
    public String gettaskmodel() {
        List<Record> taskmodel = Db.find("SELECT * FROM t_taskmodel");
        return JsonKit.toJson(taskmodel);
    }

    /***
     * 新增任务模板
     */
    public boolean addtaskmodel(int type, String apply, String userid, String applyname) {
        boolean res = new TaskModel().set("type", type).set("apply", apply).set("userid", userid).set("applyname", applyname).save();
        return res;
    }

    /***
     * 获取装置列表
     */
    public String getdeptlist() {
        List<Record> workplacename = Db.find("SELECT * FROM `t_workplacename`");
        return JsonKit.toJson(workplacename);
    }

    /***
     * 新增装置
     */
    public boolean adddept(String name) {
        boolean res = new WorkplaceModel().set("workname", name).save();
        return res;
    }
}
