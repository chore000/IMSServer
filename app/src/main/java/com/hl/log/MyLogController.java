package com.hl.log;

import com.hl.model.Mylogs;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;

public class MyLogController extends Controller {
   static LogServices logServices = new LogServices();

    public void index() {
        renderText("this is Log.class");
    }

    public void showlogsbytype() {
        String pagenum = getPara("pagenum","1");
        String pagesize = getPara("pagesize","10");
        String type = getPara("type");
        Page<Mylogs> logs = logServices.mylogsbytype(Integer.parseInt(pagenum), Integer.parseInt(pagesize), type);
        renderText(JsonKit.toJson(logs));

    }

}
