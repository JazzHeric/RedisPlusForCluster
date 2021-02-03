package com.maxbill.base.service;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.maxbill.base.bean.HashSlotRange;
import com.maxbill.base.consts.Constant;
import com.maxbill.tool.ClusterUtil;
import com.maxbill.tool.DataUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * @author: chi.zhang
 * @date: created in 2021/2/2 14:55
 * @description:
 */
@Service("clusterService")
public class ClusterService {

    @Autowired
    private AsyncService asyncService;

    public Set<String> getAllClusterKeys() {
        Jedis jedis = DataUtil.getCurrentJedisObject();
        List<HashSlotRange> clusterSlotRanges = ClusterUtil.getClusterSlotRange(jedis);
        Set<String> allKeySet = Sets.newHashSet();
        final List<ListenableFuture<Set<String>>> futures = new ArrayList<>();
        clusterSlotRanges.stream().forEach(e -> {
            for(int i = e.getMinSlotNo().intValue(); i <= e.getMaxSlotNo().intValue(); i++) {
                 futures.add((ListenableFuture<Set<String>>)asyncService.getClusterKeysInSlot(i));
            }
        });
        ListenableFuture<List<Set<String>>> listListenableFuture = Futures.successfulAsList(futures);
        List<Set<String>> resultList = null;
        try {
            resultList = listListenableFuture.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Optional.ofNullable(resultList).orElse(Collections.emptyList()).stream().forEach(e -> allKeySet.addAll(e));
        Set<String> handleKeySet = allKeySet.stream().map(e -> {
            if (e.indexOf(Constant.groupSignal) < 0) {
                e = new StringJoiner("").add(Constant.notGroupKeyPrefix).add(e).toString();
            }
            return e;
        }).collect(Collectors.toSet());
        Constant.allClusterKeySet.clear();
        Constant.allClusterKeySet.addAll(handleKeySet);
        return handleKeySet;
    }


    public Set<String> searchKeySet(final String pattern) {
        Set<String> filterResult = Optional.ofNullable(Constant.allClusterKeySet).orElse(Collections.emptySet())
                .stream().filter(e -> e.indexOf(pattern) > -1).collect(Collectors.toSet());
        return filterResult;
    }

}
