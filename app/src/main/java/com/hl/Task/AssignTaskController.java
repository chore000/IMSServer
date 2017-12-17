package com.hl.Task;

import com.jfinal.core.Controller;

public class AssignTaskController extends Controller {
    private static AssignTaskService assignTaskService = new AssignTaskService();

    public void show() {
        String taskid = getPara("taskid");
        String res = assignTaskService.gettasklogbytaskid(taskid, 1, 30);
        renderText(res);
    }
}
