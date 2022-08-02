package com.tianli.task;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-02
 **/
@Data
@AllArgsConstructor
public class RetryTaskInfo<T> {

    /**
     * 任务名称，将会拼接在threadFactory的name里
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String taskDes;

    /**
     * 任务关键参数
     */
    private T param;

    public RetryTaskInfo(String taskName){
        this.taskName = taskName;
    }
}
