package com.maxbill.core.config;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

/** 
 * 回调线程池
 * @author chi.zhang
 * @date 2020/12/3 10:13
 */
public class ListeningThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

	@Override
	public Future<?> submit(Runnable task) {
		ListeningExecutorService executor = MoreExecutors.listeningDecorator(getThreadPoolExecutor());
		try {
			return executor.submit(task);
		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		ListeningExecutorService executor = MoreExecutors.listeningDecorator(getThreadPoolExecutor());

		try {
			return executor.submit(task);

		} catch (RejectedExecutionException ex) {
			throw new TaskRejectedException("Executor [" + executor + "] did not accept task: " + task, ex);
		}
	}
}
