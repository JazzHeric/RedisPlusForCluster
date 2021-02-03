package com.maxbill.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import com.maxbill.base.consts.Constant;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 线程池配置
 * @author: chi.zhang
 * @date: created in 2021/2/2 11:09
 * @description:
 */
@Configuration
@EnableAsync
public class ExecutorConfig {

    public static final String CRC_EXECUTOR_NAME = "KEYS_IN_SLOT";

    @Bean(CRC_EXECUTOR_NAME)
    public Executor listClusterKeysExecutor() {
        ListeningThreadPoolTaskExecutor executor = new ListeningThreadPoolTaskExecutor();
        executor.setCorePoolSize(Constant.corePoolSize);
        executor.setMaxPoolSize(Constant.maxPoolSize);
        executor.setQueueCapacity(Constant.queueCapacity);
        executor.setAllowCoreThreadTimeOut(Constant.allowCoreThreadTimeout);
        executor.setKeepAliveSeconds(Constant.keepAliveSeconds);
        executor.setThreadNamePrefix(CRC_EXECUTOR_NAME);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
