package com.hl.User;

import com.hl.model.Usertype;
import com.jfinal.kit.JsonKit;

public class UserService {
    private static Usertype usertypedao = new Usertype();

    /***
     * 更新用户信息
     * @param userid
     * @param type
     * @param lvl
     * @return
     */
    public String updatetasktype(String userid, String type, String lvl) {
        Usertype usertype = usertypedao.findFirst("SELECT t_usertype.id, t_usertype.userid, t_usertype.type, t_usertype.`level`, t_usertype.stat FROM t_usertype WHERE t_usertype.userid = ? AND 1 = 1", userid);

        if (usertype == null) {
            new Usertype().set("userid", userid).set("type", type).set("level", lvl).set("stat", "1").save();
        } else if ("0".equals(usertype.getInt("stat") + "")) {
            usertype.set("level", lvl).set("type", type).set("stat","1").update();
        }
        Usertype newusertype = usertypedao.findFirst("SELECT t_usertype.id, t_usertype.userid, t_usertype.type, t_usertype.`level`, t_usertype.stat FROM t_usertype WHERE t_usertype.userid = ? AND 1 = 1", userid);

        return JsonKit.toJson(newusertype);
    }

    /**
     * 获取用户信息
     * @param userid
     * @return
     */
    public String gettasktype(String userid) {
        Usertype usertype = usertypedao.findFirst("SELECT t_usertype.id, t_usertype.userid, t_usertype.type, t_usertype.`level`, t_usertype.stat FROM t_usertype WHERE t_usertype.userid = ? AND 1 = 1", userid);
        return JsonKit.toJson(usertype);
    }

}
