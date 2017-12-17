package com.hl.util;


import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by dell on 2016/11/15.
 */
public class MemCacheHelper {


    public String get(String key) throws Exception {
        String value=null;
        MemcachedClientBuilder builder = new XMemcachedClientBuilder(
                AddrUtil.getAddresses("10.254.2.23:11211"));
        MemcachedClient memcachedClient = builder.build();
        try {
            value = memcachedClient.get(key);
            System.out.println("key=" + key+";value="+value);

        } catch (MemcachedException e) {
            System.err.println("MemcachedClientoperationfail");
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.err.println("MemcachedClientoperationtimeout");
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            memcachedClient.shutdown();

            System.out.println("close memcache");
        } catch (IOException e) {
            System.err.println("ShutdownMemcachedClientfail");
            e.printStackTrace();
        }
        return value;
    }
    public String Set(String key, String value, int exp) throws Exception {

        MemcachedClientBuilder builder = new XMemcachedClientBuilder(
                AddrUtil.getAddresses("10.254.2.23:11211"));
        MemcachedClient memcachedClient = builder.build();
        try {
            memcachedClient.add(key, exp, value);
            System.out.println("key=" + key+";value="+value);

        } catch (MemcachedException e) {
            System.err.println("MemcachedClientoperationfail");
            e.printStackTrace();
        } catch (TimeoutException e) {
            System.err.println("MemcachedClientoperationtimeout");
            e.printStackTrace();
        } catch (InterruptedException e) {
        }
        try {
            memcachedClient.shutdown();
            System.out.println("close memcache");
        } catch (IOException e) {
            System.err.println("ShutdownMemcachedClientfail");
            e.printStackTrace();
        }
        return key;
    }



}



