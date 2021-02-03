package com.maxbill.tool;

import com.maxbill.base.bean.Connect;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.Map;

public class DataUtil {

    private static Map<String, Object> confMap = new HashMap<>();

    static {
        confMap.put("currentOpenConnect", null);
        confMap.put("currentJedisObject", null);
        confMap.put("jedisClusterObject", null);
        confMap.put("outLogWindowStatus", false);
    }

    public DataUtil() {
    }

    public static void setConfig(String key, Object value) {
        confMap.put(key, value);
    }

    public static void clearConfig() {
        confMap.clear();
    }

    private static Object getConfig(String key) {
        return confMap.get(key);
    }

    public static Connect getCurrentOpenConnect() {
        return (Connect) DataUtil.getConfig("currentOpenConnect");
    }

    public static Jedis getCurrentJedisObject() {
        return (Jedis) DataUtil.getConfig("currentJedisObject");
    }

    public static JedisCluster getJedisClusterObject() {
        return (JedisCluster) DataUtil.getConfig("jedisClusterObject");
    }

    public static Boolean getOutLogWindowStatus() {
        return (Boolean) DataUtil.getConfig("outLogWindowStatus");
    }

    public static void setOutLogWindowStatus(Boolean status) {
        DataUtil.setConfig("outLogWindowStatus", status);
    }

}
