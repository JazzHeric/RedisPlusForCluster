package com.maxbill.base.controller;

import com.alibaba.fastjson.JSON;
import com.maxbill.base.bean.Connect;
import com.maxbill.tool.RedisUtil;
import com.maxbill.tool.StringUtil;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.util.Slowlog;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.maxbill.base.bean.ResultInfo.*;
import static com.maxbill.tool.DataUtil.*;

@Component
public class InfoSinglesController {

    /**
     * 获取基础服务信息
     */
    @SuppressWarnings("unused")
    public String getBaseInfo() {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                return getOkByJson(RedisUtil.getRedisInfoList(jedis));
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }


    /**
     * 获取服务日志信息
     */
    @SuppressWarnings("unused")
    public String getLogsInfo() {
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                List<Slowlog> logs = RedisUtil.getRedisLog(jedis);
                Collections.reverse(logs);
                return getOkByJson(logs);
            } else {
                return disconnect();
            }
        } catch (Exception e) {
            return exception(e);
        }
    }


    /**
     * 监控服务内存信息
     */
    @SuppressWarnings("unused")
    public String getMemInfo() {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                String info = RedisUtil.getInfo(jedis, "memory");
                String[] memory = info.split("\n");
                String val01 = StringUtil.getValueString(":", memory[1]).replace("\r", "");
                String val02 = StringUtil.getValueString(":", memory[4]).replace("\r", "");
                resultMap.put("val01", (float) (Math.round((Float.valueOf(val01) / 1048576) * 100)) / 100);
                resultMap.put("val02", (float) (Math.round((Float.valueOf(val02) / 1048576) * 100)) / 100);
            } else {
                resultMap.put("val01", 0);
                resultMap.put("val02", 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("val01", 0);
            resultMap.put("val02", 0);
        }
        return JSON.toJSONString(resultMap);
    }


    /**
     * 监控服务处理器信息
     */
    @SuppressWarnings("unused")
    public String getCpuInfo() {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                String info = RedisUtil.getInfo(jedis, "cpu");
                String[] cpu = info.split("\n");
                String val01 = StringUtil.getValueString(":", cpu[1]).replace("\r", "");
                String val02 = StringUtil.getValueString(":", cpu[2]).replace("\r", "");
                resultMap.put("val01", Float.valueOf(val01));
                resultMap.put("val02", Float.valueOf(val02));
            } else {
                resultMap.put("val01", 0);
                resultMap.put("val02", 0);
            }
        } catch (Exception e) {
            resultMap.put("val01", 0);
            resultMap.put("val02", 0);
        }
        return JSON.toJSONString(resultMap);
    }


    /**
     * 监控服务数据库信息
     */
    @SuppressWarnings("unused")
    public String getKeyInfo() {
        Long[] keys;
        try {
            Connect connect = getCurrentOpenConnect();
            Jedis jedis = getCurrentJedisObject();
            if (null != connect && null != jedis) {
                String role = RedisUtil.getInfo(jedis, "server");
                boolean isCluster = role.contains("redis_mode:cluster");
                if (isCluster) {
                    keys = new Long[1];
                    keys[0] = RedisUtil.dbSize(jedis, null);
                } else {
                    int dbCount = RedisUtil.getDbCount(jedis);
                    keys = new Long[dbCount];
                    for (int i = 0; i < dbCount; i++) {
                        keys[i] = RedisUtil.dbSize(jedis, i);
                    }
                }
            } else {
                keys = new Long[16];
                for (int i = 0; i < 16; i++) {
                    keys[i] = 0L;
                }
            }
        } catch (Exception e) {
            keys = new Long[16];
            for (int i = 0; i < 16; i++) {
                keys[i] = 0L;
            }
        }
        return JSON.toJSONString(keys);
    }


    /**
     * 监控服务网络信息
     */
    @SuppressWarnings("unused")
    public String getNetInfo() {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Jedis jedis = getCurrentJedisObject();
            if (null != jedis) {
                String info = RedisUtil.getInfo(jedis, "stats");
                String[] stats = info.split("\n");
                String val01 = StringUtil.getValueString(":", stats[6]).replace("\r", "");
                String val02 = StringUtil.getValueString(":", stats[7]).replace("\r", "");
                resultMap.put("val01", Float.valueOf(val01));
                resultMap.put("val02", Float.valueOf(val02));
            } else {
                resultMap.put("val01", 0);
                resultMap.put("val02", 0);
            }
        } catch (Exception e) {
            resultMap.put("val01", 0);
            resultMap.put("val02", 0);
        }
        return JSON.toJSONString(resultMap);
    }

}

