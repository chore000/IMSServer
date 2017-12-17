package com.hl.common;

import com.alibaba.dingtalk.openapi.demo.OApiException;
import com.alibaba.dingtalk.openapi.demo.auth.AuthHelper;
import com.alibaba.dingtalk.openapi.demo.department.DepartmentHelper;
import com.alibaba.dingtalk.openapi.demo.user.UserHelper;
import com.alibaba.dingtalk.openapi.demo.utils.HttpHelper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.open.client.api.model.corp.CorpUserDetail;
import com.dingtalk.open.client.api.model.corp.Department;
import com.hl.model.Deptmodel;
import com.hl.model.DingUser;
import com.hl.util.MD5Helper;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CommonServices {
    Logger log = Logger.getLogger(this.getClass());
    Cache userredis = Redis.use("userredis");
    Deptmodel dao = new Deptmodel();
    DingUser userdao = new DingUser();

    /***
     * 根据CODE登录
     * @param code
     * @param corpId
     * @param access_token
     * @return 返回access_token
     */
    public String loginbycode(String code, String corpId, String access_token) {
        String userjsons = userredis.get(access_token);
        if ("".equals(userjsons) || userjsons == null) {
            try {
                String accessToken = AuthHelper.getAccessToken();
                log.info("access token:" + accessToken);
//                System.out.println();
                CorpUserDetail user = UserHelper.getUser(
                        accessToken, UserHelper.getUserInfo(accessToken, code)
                                .getUserid());
                access_token = MD5Helper.EncoderByMd5(user.getUserid() + new Date());
                String userJson = JSON.toJSONString(user);
                System.out.println("userjson:" + userJson);
                userredis.setex(access_token, 6000, userJson);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        return access_token;
    }

    /***
     * 部门信息—+人员信息同步
     * @return
     */
    public boolean updatedept() {
        boolean res = false;
        List<Department> departments = new ArrayList<Department>();
//        String department_id = "3363706";
        try {
            String accessToken = AuthHelper.getAccessToken();
            departments = DepartmentHelper.listDepartments(accessToken, "1");
            for (Department department : departments) {
                Thread.sleep(100);
                // 获取部门成员
                Deptmodel deptmodel = dao.findById(department.getId());
                if (deptmodel != null) {
                    res = deptmodel.set("name", department.getName()).set("parentid", department.getParentid()).set("createdeptgroup", department.getCreateDeptGroup()).set("autoAddUser", department.getAutoAddUser()).update();
                } else {
                    res = new Deptmodel().set("id", department.getId()).set("name", department.getName()).set("parentid", department.getParentid()).set("createdeptgroup", department.getCreateDeptGroup()).set("autoAddUser", department.getAutoAddUser()).save();
                }
                JSONObject jb = HttpHelper.httpGet("https://oapi.dingtalk.com/user/list?access_token=" + accessToken + "&department_id=" + department.getId());
                JSONArray ja = JSONArray.parseArray(jb.get("userlist") + "");
                for (int i = 0; i < ja.size(); i++) {
                    JSONObject person = JSONObject.parseObject(ja.get(i) + "");
                    if (person.getBoolean("isLeader")) {
                        System.out.println(person.getString("name"));
                        int s = Db.update("UPDATE t_ding_dept SET admin=? WHERE id=?", person.getString("userid"), department.getId());
                        log.info(department.getId() + "部门负责人更新为" + person.getString("userid") + "状态为" + s);

                    }
                    DingUser usercountr = userdao.findFirst("SELECT * FROM t_ding_user WHERE userid=?", person.getString("userid"));
                    if (usercountr != null) {
                        usercountr.set("jobnum", person.getString("jobnumber")).set("avatar", person.getString("avatar")).set("department", person.getString("department")).set("isadmin", person.getBoolean("isAdmin")).set("name", person.getString("name")).update();
//            int s = Db.update("UPDATE t_ding_user SET jobnum=?,active=1,avatar=?,department=?,isAdmin=?,name=? WHERE userid=?", user.getJobnumber(), user.getAvatar(), realdept.get(0).getInt("id"), user.getIsAdmin(), user.getName(), user.getUserid());
                        log.info("用户状态更新：用户id" + person.getString("userid"));
                    } else {
                        new DingUser().set("userid", person.getString("userid")).set("jobnum", person.getString("jobnumber")).set("avatar", person.getString("avatar")).set("department", person.getString("department")).set("isadmin", person.getBoolean("isAdmin")).set("name", person.getString("name")).save();
//            Db.update("INSERT INTO t_ding_user( userid ,  avatar ,  department ,  isAdmin ,name, jobnum, active)VALUES  ( ? , ? , ? , ? ,?,?,1 )", user.getUserid(), user.getAvatar(), realdept.get(0).getInt("id"), user.getIsAdmin(), user.getName(), user.getJobnumber());
                        log.info("用户插入：用户id" + person.getString("userid"));
                    }

                }
            }

        } catch (OApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }




   /* public String updateuserinfo(CorpUserDetail user) {
        String userid = user.getUserid();
        String isleader = user.getIsLeaderInDepts();
        if (isleader != null) {
            isleader = isleader.substring(1, isleader.length() - 1);
            System.out.println(isleader);
            String[] leaders = isleader.split(",");
            for (String leader : leaders) {
                String[] leaderstru = leader.split(":");
                if ("true".equals(leaderstru[1])) {
                    int s = Db.update("UPDATE t_ding_dept SET admin=? WHERE id=?", userid, leaderstru[0]);
                    log.info(leaderstru[0] + "部门负责人更新为" + userid + "状态为" + s);
                }
            }
        }
        List<Integer> depts = new ArrayList<>(user.getDepartment().size());
        //查找有部门群的部门
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM t_ding_dept WHERE createdeptgroup=1 AND id IN(");

        for (int i = 0; i < user.getDepartment().size(); i++) {
            sb.append(user.getDepartment().get(i) + ",");
        }
        sb.append("-1)");
        List<Record> realdept = Db.find(sb.toString());
//        System.out.println(realdept.get(0).getInt("id"));
        DingUser usercountr = userdao.findFirst("SELECT * FROM t_ding_user WHERE userid=?", userid);
        if (usercountr != null) {
            usercountr.set("jobnum", user.getJobnumber()).set("avatar", user.getAvatar()).set("department", JsonKit.toJson(user.getDepartment())).set("isadmin", user.getIsAdmin()).set("name", user.getName()).update();
//            int s = Db.update("UPDATE t_ding_user SET jobnum=?,active=1,avatar=?,department=?,isAdmin=?,name=? WHERE userid=?", user.getJobnumber(), user.getAvatar(), realdept.get(0).getInt("id"), user.getIsAdmin(), user.getName(), user.getUserid());
            log.info("用户状态更新：部门" + user.getDepartment().toString() + "负责人" + user.getIsAdmin() + "用户id" + user.getUserid());
        } else {
            new DingUser().set("userid", user.getUserid()).set("jobnum", user.getJobnumber()).set("avatar", user.getAvatar()).set("department", JsonKit.toJson(user.getDepartment())).set("isadmin", user.getIsAdmin()).set("name", user.getName()).save();
//            Db.update("INSERT INTO t_ding_user( userid ,  avatar ,  department ,  isAdmin ,name, jobnum, active)VALUES  ( ? , ? , ? , ? ,?,?,1 )", user.getUserid(), user.getAvatar(), realdept.get(0).getInt("id"), user.getIsAdmin(), user.getName(), user.getJobnumber());
            log.info("用户插入：部门" + user.getDepartment().toString() + "负责人" + user.getIsAdmin() + "用户id" + user.getUserid());
        }
        return null;
    }*/
}
