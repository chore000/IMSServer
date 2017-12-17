package com.hl.Task;

import com.jfinal.core.Controller;

public class TasktypeController extends Controller {
    private static TasktypeServices tasktypeServices = new TasktypeServices();

    public void gettasktype() {
        String access_token = getPara("access_token");
        String types = tasktypeServices.gettasktype(access_token);
        renderText(types);
    }

    public void gettaskArea() {
//        String access_token = getPara("access_token");
        String types = tasktypeServices.gettaskArea();
        renderText(types);
    }
    public void getlvlname() {
//        String access_token = getPara("access_token");
        String types = tasktypeServices.getlvlname();
        renderText(types);
    }
}
