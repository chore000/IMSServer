package com.alibaba.dingtalk.openapi.demo.auth;

import com.alibaba.dingtalk.openapi.demo.Env;
import com.alibaba.dingtalk.openapi.demo.OApiException;
import com.alibaba.dingtalk.openapi.demo.utils.FileUtils;
import com.alibaba.dingtalk.openapi.demo.utils.HttpHelper;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.oapi.lib.aes.DingTalkJsApiSingnature;
import com.dingtalk.open.client.ServiceFactory;
import com.dingtalk.open.client.api.model.corp.JsapiTicket;
import com.dingtalk.open.client.api.service.corp.CorpConnectionService;
import com.dingtalk.open.client.api.service.corp.JsapiService;
import com.hl.util.MemCacheHelper;
import com.jfinal.kit.PropKit;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;

/**
 * AccessToken和jsticket的获取封装
 */
public class AuthHelper {

    // 调整到1小时50分钟
    public static final int cacheTime = 60 * 55 * 2;
    private static final String corpid=PropKit.get("corpid");
    private static final String corpsecret=PropKit.get("corpsecret");

    /*
     * 在此方法中，为了避免频繁获取access_token，
     * 在距离上一次获取access_token时间在两个小时之内的情况，
     * 将直接从持久化存储中读取access_token
     *
     * 因为access_token和jsapi_ticket的过期时间都是7200秒
     * 所以在获取access_token的同时也去获取了jsapi_ticket
     * 注：jsapi_ticket是在前端页面JSAPI做权限验证配置的时候需要使用的
     * 具体信息请查看开发者文档--权限验证配置
     */
    public static String getAccessToken() throws OApiException {
        String accToken = "";
        try {
//            System.out.println("get mem");
            MemCacheHelper memCacheHelper = new MemCacheHelper();
            accToken = memCacheHelper.get("access_token_" + corpid);
        } catch (Exception e) {
            System.out.print(e.toString());
        }
        if (accToken == null || accToken == "") {
            try {
                System.out.println("调用接口获取");
                ServiceFactory serviceFactory = ServiceFactory.getInstance();
                CorpConnectionService corpConnectionService = serviceFactory.getOpenService(CorpConnectionService.class);
                accToken = corpConnectionService.getCorpToken(corpid, corpsecret);
                MemCacheHelper memCacheHelper1 = new MemCacheHelper();
                memCacheHelper1.Set("access_token_" + corpid, accToken, cacheTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("读取accToken缓存数据");
        }
        System.out.print(accToken);
        return accToken;
    }

    /**
     * 获取JSTicket, 用于js的签名计算
     * 正常的情况下，jsapi_ticket的有效期为7200秒，所以开发者需要在某个地方设计一个定时器，定期去更新jsapi_ticket
     */
    public static String getJsapiTicket(String accessToken) throws OApiException {
        String jsTicket = "";
        try {
            MemCacheHelper memCacheHelper = new MemCacheHelper();
            jsTicket = memCacheHelper.get("jsapiticket_" + corpid);
        } catch (Exception e) {
            System.out.print(e.toString());
        }

        if (jsTicket == null || jsTicket == "") {
            try {
                System.out.print("调用接口获取");
                ServiceFactory serviceFactory;
                serviceFactory = ServiceFactory.getInstance();
                JsapiService jsapiService = serviceFactory.getOpenService(JsapiService.class);
                JsapiTicket JsapiTicket = jsapiService.getJsapiTicket(accessToken, "jsapi");
                jsTicket = JsapiTicket.getTicket();
                MemCacheHelper memCacheHelper1 = new MemCacheHelper();
                memCacheHelper1.Set("jsapiticket_" + corpid, jsTicket, cacheTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.print("读取JsTicket缓存数据");
        }

        return jsTicket;

    }


    public static String sign(String ticket, String nonceStr, long timeStamp, String url) throws OApiException {
        try {
            return DingTalkJsApiSingnature.getJsApiSingnature(url, nonceStr, timeStamp, ticket);
        } catch (Exception ex) {
            throw new OApiException(0, ex.getMessage());
        }
    }


    /**
     * 计算当前请求的jsapi的签名数据<br/>
     * <p>
     * 如果签名数据是通过ajax异步请求的话，签名计算中的url必须是给用户展示页面的url
     *
     * @param request
     * @return
     */
    public static String getConfig(HttpServletRequest request) {

        String urlString = request.getRequestURL().toString();
        String queryString = request.getQueryString();

        String queryStringEncode = null;
        String url;
        if (queryString != null) {
            queryStringEncode = URLDecoder.decode(queryString);
            url = urlString + "?" + queryStringEncode;
        } else {
            url = urlString;
        }

        String nonceStr = "hailong1";
        long timeStamp = System.currentTimeMillis() / 1000;
//        String signedUrl = url;
        System.out.println("url:" + url);
        String signedUrl = PropKit.get("url");    // http://10.3.12.75:8080/
//        String signedUrl = "http://222.134.52.36:8001/";
        String accessToken = null;
        String ticket = null;
        String signature = null;
        String agentid = null;

        try {
            accessToken = AuthHelper.getAccessToken();

            ticket = AuthHelper.getJsapiTicket(accessToken);
            signature = AuthHelper.sign(ticket, nonceStr, timeStamp, signedUrl);
            agentid = PropKit.get("agentid");//"10851862";

        } catch (OApiException e) {
            e.printStackTrace();
        }
        String configValue = "{jsticket:'" + ticket + "',signature:'" + signature + "',nonceStr:'" + nonceStr + "',timeStamp:'"
                + timeStamp + "',corpId:'" + corpid + "',agentid:'" + agentid + "'}";
        System.out.println(configValue);
        return configValue;
    }


    public static String getSsoToken() throws OApiException {
        String url = "https://oapi.dingtalk.com/sso/gettoken?corpid=" + corpid + "&corpsecret=" + Env.SSO_Secret;
        JSONObject response = HttpHelper.httpGet(url);
        String ssoToken;
        if (response.containsKey("access_token")) {
            ssoToken = response.getString("access_token");
        } else {
            throw new OApiException("Sso_token");
        }
        return ssoToken;

    }

}
