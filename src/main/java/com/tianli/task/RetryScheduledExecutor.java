
package com.tianli.task;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-02
 **/
@Slf4j
public class RetryScheduledExecutor extends ScheduledThreadPoolExecutor {

    private static final AtomicInteger threadNumber = new AtomicInteger(1);

    private static final Map<Runnable, RetryTaskInfo<?>> TASK_INFO_MAP = new ConcurrentHashMap<>(128);

    public static final RetryScheduledExecutor DEFAULT_EXECUTOR = new RetryScheduledExecutor(Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread thread = new Thread(r);
                RetryTaskInfo<?> taskInfo = Optional.ofNullable(TASK_INFO_MAP.get(r)).orElse(new RetryTaskInfo<>("noname"));
                thread.setName("retry-task-" + taskInfo.getTaskName() + "-" + threadNumber.getAndIncrement());
                return thread;
            });


    public RetryScheduledExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory);
    }

    public void schedule(Runnable command, long delay, TimeUnit unit, RetryTaskInfo<?> taskInfo) {
        log.info("任务描述【{}】,将在{} {}后定时执行,执行参数【{}】", taskInfo.getTaskDes(), delay, unit.name()
                , JSONUtil.toJsonStr(taskInfo.getParam()));
        TASK_INFO_MAP.put(command, taskInfo);
        super.schedule(command, delay, unit);
        TASK_INFO_MAP.remove(command);
    }
}
