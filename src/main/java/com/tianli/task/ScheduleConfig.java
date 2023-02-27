package com.tianli.task;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ScheduledThreadPoolExecutor;


/**
 * Spring 定时任务默认为单线程，如果任务多会影响各自执行
 */
@Configuration
public class ScheduleConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(
                new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2)
        );
    }
}
