package com.hl.Intercepetor;


import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GlobalActionInterceptor implements Interceptor {
    private String allowMethods;
    private String allowHeaders;

    @Override
    public void intercept(Invocation inv) {


        HttpServletRequest request = inv.getController().getRequest();
        HttpServletResponse response = inv.getController().getResponse();


        String currentOrigin = request.getHeader("Origin");

        response.setHeader("Access-Control-Allow-Origin", currentOrigin);
        allowMethods=request.getHeader("allowMethods");
        response.setHeader("Access-Control-Allow-Methods", allowMethods);
        response.setHeader("Access-Control-Allow-Credentials", "true");
        allowHeaders=request.getHeader("allowHeaders");
        response.setHeader("Access-Control-Allow-Headers", allowHeaders);
        inv.invoke();


    }
}
