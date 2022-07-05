package com.tianli.user.logs.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import com.tianli.common.async.AsyncService;
import com.tianli.tool.IPUtils;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.logs.mapper.UserIpLog;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


@Component
public class IpAnalysisTask {

    @Resource
    private UserIpLogService userIpLogService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private AsyncService asyncService;

    private static final String IP_ANALYSIS_INIT_KEY = "IpAnalysisTask:analysis:init:key";
    private static final String IP_ANALYSIS_COUNT_KEY = "IpAnalysisTask:analysis:count:key";
    private static final String IP_ANALYSIS_STATISTICS_KEY = "IpAnalysisTask:analysis:statistics:key";
    private static final int MAX_EXECUTE_COUNT = 10;

    @Scheduled(cron = "0 0/3 * * * ?")
    public void analysis() {
        asyncService.async(() -> {
            // 获取本次执行任务的最高权限
            BoundValueOperations<String, Object> boundValueOperations = redisTemplate.boundValueOps(IP_ANALYSIS_INIT_KEY);
            Boolean lock = boundValueOperations.setIfAbsent(1, 3L, TimeUnit.MINUTES);
            try {


                // 查询允许权的总量
                BoundValueOperations<String, Object> boundValueOperations1 = redisTemplate.boundValueOps(IP_ANALYSIS_COUNT_KEY);
                Object allowCount = boundValueOperations1.get();
                long lastAllowCount = 1;
                if (Objects.nonNull(allowCount)) {
                    lastAllowCount = Math.max(lastAllowCount, Long.parseLong(allowCount.toString()));
                }

                // 预定下次的执行权
                BoundValueOperations<String, Object> boundValueOperations2 = redisTemplate.boundValueOps(IP_ANALYSIS_STATISTICS_KEY);
                if (Objects.isNull(lock) || !lock) {
                    Boolean reserve = boundValueOperations2.setIfAbsent(1, 5L, TimeUnit.MINUTES);
                    if (Objects.nonNull(reserve) && !reserve) {
                        boundValueOperations2.increment();
                    }
                    if (lastAllowCount <= 1) {
                        return;
                    }
                }
                // 获取执行分片id
                Long executeIndex;
                if (Objects.nonNull(lock) && lock) {
                    executeIndex = lastAllowCount - 1;
                } else {
                    Long decrement = boundValueOperations1.decrement();
                    if (Objects.isNull(decrement) || decrement <= 0) {
                        return;
                    }
                    executeIndex = decrement - 1;
                }

                // 执行ip解析
                {
                    int executeCount = 0;
                    long startId = 0L;
                    while (executeCount < MAX_EXECUTE_COUNT) {
                        List<UserIpLog> list = userIpLogService.list(Wrappers.lambdaQuery(UserIpLog.class)
                                .gt(UserIpLog::getId, startId)
                                .eq(UserIpLog::getState, 0)
                                .last(" LIMIT " + 5)
                        );
                        if (CollectionUtils.isEmpty(list)) {
                            break;
                        }
                        startId = list.get(list.size() - 1).getId();
                        List<UserIpLog> needUpdateList = Lists.newArrayList();
                        for (UserIpLog userIpLog : list) {
                            if (userIpLog.getId() % lastAllowCount != executeIndex) {
                                continue;
                            }
                            String ip = userIpLog.getIp();
                            try {
                                Map<String, String> ipAnalysis = IPUtils.ipAnalysis(ip);
                                needUpdateList.add(UserIpLog.builder()
                                        .id(userIpLog.getId())
                                        .update_time(LocalDateTime.now())
                                        .latitude(Objects.isNull(ipAnalysis.get("latitude")) ? 0.0 : Double.parseDouble(ipAnalysis.get("latitude")))
                                        .longitude(Objects.isNull(ipAnalysis.get("longitude")) ? 0.0 : Double.parseDouble(ipAnalysis.get("longitude")))
                                        .country(ipAnalysis.get("country"))
                                        .region(ipAnalysis.get("province"))
                                        .city(ipAnalysis.get("city"))
                                        .state(1)
                                        .build());
                                executeCount++;
                            } catch (Exception ignored) {
                            }
                        }
                        if(CollectionUtils.isEmpty(needUpdateList)){
                            continue;
                        }
                        userIpLogService.updateBatchById(needUpdateList);
                    }
                }

                // 把当前的线程也加入下次的执行权列表
                if(Objects.nonNull(lock) && lock){
                    Boolean reserve = boundValueOperations2.setIfAbsent(1, 5L, TimeUnit.MINUTES);
                    if (Objects.nonNull(reserve) && !reserve) {
                        boundValueOperations2.increment();
                    }

                    // 设置下次需要执行的总的权限令牌数
                    Object o = boundValueOperations2.get();
                    if (Objects.nonNull(o)) {
                        boundValueOperations1.set(o, 5L, TimeUnit.MINUTES);
                    }
                }

            } catch (Exception ignore) {
            } finally {
                if (Objects.nonNull(lock) && lock){
                    Boolean delete = redisTemplate.delete(IP_ANALYSIS_INIT_KEY);
                    redisTemplate.delete(IP_ANALYSIS_STATISTICS_KEY);
                    if (Objects.isNull(delete) || !delete) {
                        redisTemplate.delete(IP_ANALYSIS_INIT_KEY);
                    }
                }
            }
        });

    }
}
