package com.maxbill.base.service;

import com.google.common.collect.Sets;
import com.maxbill.core.config.ExecutorConfig;
import com.maxbill.tool.ClusterUtil;
import com.maxbill.tool.RedisUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.concurrent.Future;

/**
 * @author: chi.zhang
 * @date: created in 2021/2/2 14:13
 * @description:
 */
@Service
public class AsyncService {


    @Async(ExecutorConfig.CRC_EXECUTOR_NAME)
    public Future<Set<String>> getClusterKeysInSlot(int slot) {
        Jedis jedis = null;
        Set<String> clusterKeys = Sets.newHashSet();
        try {
            jedis = RedisUtil.getJedis();
            clusterKeys = ClusterUtil.getClusterKeysInSlot(slot, jedis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            RedisUtil.closeJedis(jedis);
        }
        return new AsyncResult<>(clusterKeys);
    }
}
