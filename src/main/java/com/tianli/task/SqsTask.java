package com.tianli.task;

import com.tianli.sqs.SqsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-28
 **/
@Component
public class SqsTask {

    @Resource
    private SqsService sqsService;

    @Scheduled(cron = "0/1 * * * * ? ")
    public void pushAddressTask() {
        sqsService.receiveAndDelete(null,8);
    }
}
