package com.zt.rpc.util;

import cn.hutool.core.net.NetUtil;

import java.net.InetAddress;

public class NetworkUtil {
    public static String getLocalIpAddress() {

        String ip = "127.0.0.1";
        InetAddress addr = NetUtil.getLocalhost();
        if (addr != null) {
            ip = addr.getHostAddress();
        }
        return ip;
    }
}
