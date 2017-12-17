package com.hl.config;

import com.hl.Intercepetor.GlobalActionInterceptor;
import com.hl.Intercepetor.LoginInterceptor;
import com.hl.Task.AssignTaskController;
import com.hl.Task.SecTaskController;
import com.hl.Task.TaskController;
import com.hl.Task.TasktypeController;
import com.hl.User.UsertypeController;
import com.hl.argu.ArguController;
import com.hl.common.HelloController;
import com.hl.log.MyLogController;
import com.hl.message.MsgController;
import com.hl.model.*;
import com.hl.taskaplly.TaskApplyController;
import com.jfinal.config.*;
import com.jfinal.ext.handler.UrlSkipHandler;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jfinal.plugin.ehcache.EhCachePlugin;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;

import java.util.Timer;

/**
 * Created by dell on 2017/2/16.
 */
public class MyAppconfig extends JFinalConfig {
    private Timer timer = new Timer();

    @Override
    public void configConstant(Constants me) {
        PropKit.use("jfinal.properties");
        me.setDevMode(PropKit.getBoolean("DevMode"));
        me.setEncoding("utf-8");
        me.setViewType(ViewType.JSP);

        me.setBaseUploadPath("F://改善系统//改善图片//");
        me.setBaseDownloadPath("F://改善系统//改善图片//");
        me.setMaxPostSize(100 * 1024 * 1024);
    }

    @Override
    public void configRoute(Routes me) {

        me.add("/", HelloController.class);
        me.add("/task", TaskController.class);
        me.add("/argu", ArguController.class);
        me.add("/log", MyLogController.class);
        me.add("/assignlog", AssignTaskController.class);
        me.add("/tasktype", TasktypeController.class);
        me.add("/taskapply", TaskApplyController.class);
        me.add("/usertype", UsertypeController.class);
        me.add("/sectask", SecTaskController.class);
        me.add("/msg", MsgController.class);


    }


    @Override
    public void configEngine(Engine me) {

    }

    @Override
    public void configPlugin(Plugins me) {

        DruidPlugin cp = new DruidPlugin(PropKit.get("jdbcUrl"),
                PropKit.get("user"), PropKit.get("password")); // 使用C3P0
        me.add(cp);
        ActiveRecordPlugin arp = new ActiveRecordPlugin(cp);
        arp.setShowSql(true);
        me.add(arp);

    /*    ActivitiPlugins apg = new ActivitiPlugins();
        me.add(apg);*/
//        SQLServerDataSource dataSource = new SQLServerDataSource();
        RedisPlugin loginuserredis = new RedisPlugin("userredis", PropKit.get("redispath"));
        me.add(loginuserredis);
        me.add(new EhCachePlugin());


    }

    @Override
    public void configInterceptor(Interceptors me) {
        // 添加控制层全局拦截器
        me.addGlobalActionInterceptor(new GlobalActionInterceptor());
        me.addGlobalActionInterceptor(new LoginInterceptor());
    }

    @Override
    public void configHandler(Handlers me) {
        me.add(new UrlSkipHandler("(^/websocket).*?",false));
    }


}
