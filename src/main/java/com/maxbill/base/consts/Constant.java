package com.maxbill.base.consts;

import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * @author: chi.zhang
 * @date: created in 2021/2/2 11:05
 * @description:
 */
public interface Constant {

    int treePageSize = 9999999;

    int corePoolSize = 40;

    int maxPoolSize = 50;

    int queueCapacity = 200;

    int keepAliveSeconds = 180;

    boolean allowCoreThreadTimeout = true;

    String groupSignal = ":";

    String notGroupKeyPrefix = "_NOT_GROUPED:";

    Set<String> allClusterKeySet = Sets.newHashSet();
}
