package com.tianli.kline.task;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.tianli.kline.KLineConstants;
import com.tianli.tool.time.TimeTool;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum HuoBiPeriodEnum {
    onemin("1min", 10, 60 * 1000L, 709.66, 709.02, 0.47),
    fivemin("5min", 60, 5 * 60 * 1000L, 678.98, 682.20, -4.51),
    fifteenmin("15min", 5 * 60, 15 * 60 * 1000L, 644.76, 643.72, 0.98),
    thirtymin("30min", 5 * 60, 30 * 60 * 1000L, 615.17, 610.32, 2.58),
    sixtymin("60min", 5 * 60, 60 * 60 * 1000L, 592.48, 601.98, 1.92),
    /**
     * 暂时去除不支持类型 4小时, 一周, 一月, 一年的维度
     */
    //    fourhour("4hour", 5 * 60, 4 * 60 * 60 * 1000L, 592.48, 601.98, 1.92),
    oneday("1day", 60 * 60, 24 * 60 * 60 * 1000L, 375.32, 386.14, -4.10),
    //    oneweek("1week", 24 * 60 * 60, 7 * 24 * 60 * 60 * 1000L, 375.32, 386.14, -4.10),
    //    onemonth("1mon", 3 * 24 * 60 * 60, -1L, 375.32, 386.14, -4.10),
    //    oneyear("1year", 3 * 24 * 60 * 60, -2L, 375.32, 386.14, -4.10)
    ;
    private String des;
    // 单位: 秒
    private int durationTime;
    // 时间ms的容量: 1min => 60 * 1000ms
    private long capacity;
    // 起始点的ema12, ema26, dea的默认数据值
    private double ema12;
    private double ema26;
    private double dea;

    HuoBiPeriodEnum(String des, int durationTime, long capacity
            , double ema12, double ema26, double dea) {
        this.des = des;
        this.durationTime = durationTime;
        this.capacity = capacity;
        this.ema12 = ema12;
        this.ema26 = ema26;
        this.dea = dea;
    }

    public void increment(BoundHashOperations<String, Object, Object> hashOperations, long time, double jump) {
        long remainder = time % capacity;
        long realKey;
        if(this.capacity == -1){
            realKey = TimeTool.minMonthTime(TimeTool.getDateTimeOfTimestamp(time)).toEpochSecond(ZoneOffset.of("+8"));
        } else if(this.capacity == -2){
            realKey = TimeTool.minYearTime(TimeTool.getDateTimeOfTimestamp(time)).toEpochSecond(ZoneOffset.of("+8"));
        } else {
            realKey = (time - remainder);
        }
        hashOperations.increment(realKey / 1000, jump);
    }

    public String getStatKey(String prefix) {
       return String.format("%s_%s", prefix, this.getDes());
    }

    /**
     * 同步执行: 押注统计数据
     *
     * @param redisTemplate redis客户端
     * @param prefix key前缀
     * @param time 时间戳
     * @param step 步长
     */
    public static void incrementAll(RedisTemplate<String, Object> redisTemplate, String prefix, long time, double step) {
        for (HuoBiPeriodEnum e : HuoBiPeriodEnum.values()){
            String key = e.getStatKey(prefix);
            BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
            e.increment(operations, time, step);
        }
    }

    /**
     * Pipeline 执行: 押注统计数据
     *
     * @param redisTemplate redis客户端
     * @param prefix key前缀
     * @param time 时间戳
     * @param step 步长
     */
    public static void incrementAllPipelined(RedisTemplate<String, Object> redisTemplate, String prefix, long time, double step) {
        HuoBiPeriodEnum[] huoBiPeriodEnums = HuoBiPeriodEnum.values();
        List<PipelineExe> pipelineList = Lists.newArrayListWithCapacity(huoBiPeriodEnums.length);
        for (HuoBiPeriodEnum e : huoBiPeriodEnums){
            String key = e.getStatKey(prefix);
            long remainder = time % e.capacity;
            long realKey;
            if(e.capacity == -1){
                realKey = TimeTool.minMonthTime(TimeTool.getDateTimeOfTimestamp(time)).toEpochSecond(ZoneOffset.of("+8"));
            } else if(e.capacity == -2){
                realKey = TimeTool.minYearTime(TimeTool.getDateTimeOfTimestamp(time)).toEpochSecond(ZoneOffset.of("+8"));
            } else {
                realKey = (time - remainder);
            }
            if(Objects.equals(e, HuoBiPeriodEnum.oneday)){
                realKey = realKey - 28800_000;
            }
            PipelineExe pipelineExe = new PipelineExe();
            pipelineExe.setKey(key);
            pipelineExe.setTime(realKey/ 1000);
            pipelineExe.setStep(step);
            pipelineList.add(pipelineExe);
        }
        //使用pipeline方式
        redisTemplate.executePipelined((RedisCallback<List<Object>>) connection -> {
            for (PipelineExe exe : pipelineList) {
                StringRedisSerializer keySerializer = (StringRedisSerializer)redisTemplate.getKeySerializer();
                GenericJackson2JsonRedisSerializer hashKeySerializer = (GenericJackson2JsonRedisSerializer)redisTemplate.getHashKeySerializer();
                byte[] rawKey = keySerializer.serialize(exe.getKey());
                byte[] rawTime = hashKeySerializer.serialize(exe.getTime());
                if(Objects.nonNull(rawKey) && Objects.nonNull(rawTime) && exe.getStep() != 0){
                    connection.hIncrBy(rawKey, rawTime, exe.getStep());
                }
            }
            return null;
        });
    }

    @Data
    static class PipelineExe {
        private String key;
        private long time;
        private double step;
    }

    public KlineMacd getMacdCache(StringRedisTemplate stringRedisTemplate) {
        BoundValueOperations<String, String> boundValueOps = stringRedisTemplate.boundValueOps(KLineConstants.MACD_CACHE_REDIS_KEY);
        String cacheMacd = boundValueOps.get();
        Gson gson = new Gson();
        KlineMacd klineMacd;
        if(StringUtils.isNotBlank(cacheMacd)){
            klineMacd = gson.fromJson(cacheMacd, KlineMacd.class);
        }else{
            klineMacd = new KlineMacd();
            klineMacd.setEma12(Stream.of(HuoBiPeriodEnum.values()).collect(Collectors.toMap(HuoBiPeriodEnum::getDes, HuoBiPeriodEnum::getEma12)));
            klineMacd.setEma26(Stream.of(HuoBiPeriodEnum.values()).collect(Collectors.toMap(HuoBiPeriodEnum::getDes, HuoBiPeriodEnum::getEma26)));
            klineMacd.setDea(Stream.of(HuoBiPeriodEnum.values()).collect(Collectors.toMap(HuoBiPeriodEnum::getDes, HuoBiPeriodEnum::getDea)));
            boundValueOps.setIfAbsent(gson.toJson(klineMacd));
        }
        return klineMacd;
    }

    public KlineMacd flushMacdCache(StringRedisTemplate stringRedisTemplate, KlineMacd klineMacd) {
        BoundValueOperations<String, String> boundValueOps = stringRedisTemplate.boundValueOps(KLineConstants.MACD_CACHE_REDIS_KEY);
        Gson gson = new Gson();
        boundValueOps.set(gson.toJson(klineMacd));
        return klineMacd;
    }
}
