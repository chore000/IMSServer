package com.hl.bounspoint;

import com.hl.model.BpModel;
import com.jfinal.plugin.activerecord.Page;

public class BPServices {
    private static BpModel bpdao = new BpModel();

    /***
     * 添加积分
     * @param userid
     * @param type
     * @param stat
     * @param taskid
     * @param remark
     */
    public void addbp(String userid, String type, String stat, String taskid, String remark, String marks) {
        new BpModel().set("userid", userid).set("type", type).set("stat", stat).set("taskid", taskid).set("remark", remark).set("marks", marks).save();
    }


    public void editbp() {
    }

    /***
     * 获取今日积分通过userid
     * @param userid
     */
    public void getbpbmarkyuseridtoday(String userid) {
    }

    /***
     * 获取本月积分通过userid
     * @param month
     * @param userid
     */
    public void getbpbyuseridmonthtoday(String userid, String month) {
    }

    /***
     * 获取年度积分通过userid
     * @param userid
     * @param year
     */
    public void getbpbyuseridbyyear(String userid, String year) {
    }

    /***
     * 获取积分列表通过userid
     * @param userid
     */
    public void getbpbyuserid(int pagenum, int pagesize, String userid) {
        Page<BpModel> bps = bpdao.paginate(pagenum, pagesize, "", "", userid);
    }
}
