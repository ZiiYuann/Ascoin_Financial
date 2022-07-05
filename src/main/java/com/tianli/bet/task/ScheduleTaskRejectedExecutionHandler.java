package com.tianli.bet.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 自定义线程池拒绝策略
 *
 * 主要任务打印详细的任务信息
 */
public class ScheduleTaskRejectedExecutionHandler implements RejectedExecutionHandler {

    private static final Logger log = LoggerFactory.getLogger(ScheduleTaskRejectedExecutionHandler.class);

    private static volatile ScheduleTaskRejectedExecutionHandler rejectedExecutionHandler = null;
    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // 特出情况处理: 落异常库/消息队列等
        log.warn("押注更新任务过多!!! [Task " + r.toString() + " rejected from " + executor.toString() + "]");
    }
    public static ScheduleTaskRejectedExecutionHandler getInstance(){
        if(Objects.nonNull(rejectedExecutionHandler)){
            return rejectedExecutionHandler;
        }
        synchronized (ScheduleTaskRejectedExecutionHandler.class){
            if(Objects.nonNull(rejectedExecutionHandler)){
                return rejectedExecutionHandler;
            }
            rejectedExecutionHandler = new ScheduleTaskRejectedExecutionHandler();
        }
        return rejectedExecutionHandler;
    }
}
