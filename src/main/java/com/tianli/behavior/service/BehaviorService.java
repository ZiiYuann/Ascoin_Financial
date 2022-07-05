package com.tianli.behavior.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.behavior.dao.BehaviorMapper;
import com.tianli.behavior.dto.BehaviorAddDto;
import com.tianli.behavior.entity.Behavior;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * @author lzy
 * @date 2022/5/16 15:35
 */
@Service
public class BehaviorService extends ServiceImpl<BehaviorMapper, Behavior> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RequestInitService requestInitService;

    private final String FIRST_ACCESS_KEY = "FIRST_ACCESS_KEY_";

    private final String FIRST_DAY_ACCESS_KEY = "FIRST_DAY_ACCESS_KEY_";


    public void add(BehaviorAddDto behaviorAddDto) {
        setFirst(behaviorAddDto);
        Behavior behavior = BeanUtil.copyProperties(behaviorAddDto, Behavior.class);
        behavior.setId(CommonFunction.generalId());
        behavior.setCreate_time(LocalDateTime.now());
        this.save(behavior);
    }

    private BehaviorAddDto setFirst(BehaviorAddDto behaviorAddDto) {
        Long uid = requestInitService.get().getUid();
        if (ObjectUtil.isNotNull(uid)) {
            behaviorAddDto.setUser_id(uid.toString());
        }
        String userId = behaviorAddDto.getUser_id();
        String device_id = behaviorAddDto.getDevice_id();
        Boolean is_first_day = Boolean.FALSE;
        Boolean is_first_time = Boolean.FALSE;
        if (StrUtil.isBlank(userId) && StrUtil.isBlank(device_id)) {
            behaviorAddDto.setIs_first_day(false);
            behaviorAddDto.setIs_first_time(false);
            return behaviorAddDto;
        }
        if (StrUtil.isBlank(userId)) {
            Object data = redisTemplate.boundValueOps(FIRST_ACCESS_KEY + device_id).get();
            if (ObjectUtil.isNull(data)) {
                int count = this.count(Wrappers.lambdaQuery(Behavior.class).eq(Behavior::getDevice_id, device_id));
                if (count <= 0) {
                    is_first_day = Boolean.TRUE;
                    is_first_time = Boolean.TRUE;
                    setKey(FIRST_ACCESS_KEY, device_id);
                    setKey(FIRST_DAY_ACCESS_KEY, device_id);
                }
            } else {
                is_first_day = isFirstDay(device_id);
            }
        } else {
            //userId不为空肯定不是首次访问 直接判断首日访问
            is_first_day = isFirstDay(userId);
        }
        behaviorAddDto.setIs_first_day(is_first_day);
        behaviorAddDto.setIs_first_time(is_first_time);
        return behaviorAddDto;
    }

    private Boolean isFirstDay(String id) {
        Boolean is_first_day = Boolean.FALSE;
        Object date = redisTemplate.boundValueOps(FIRST_DAY_ACCESS_KEY + id).get();
        if (ObjectUtil.isNull(date)) {
            is_first_day = Boolean.TRUE;
            setKey(FIRST_DAY_ACCESS_KEY, id);
        } else if (!StrUtil.equals(date.toString(), LocalDate.now().toString())) {
            is_first_day = Boolean.TRUE;
            setKey(FIRST_DAY_ACCESS_KEY, id);
        }
        return is_first_day;
    }

    private void setKey(String key, String id) {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(key + id);
        if (StrUtil.equals(FIRST_ACCESS_KEY, key)) {
            ops.set(id, 7L, TimeUnit.DAYS);
        } else {
            ops.set(LocalDate.now().toString(), 1L, TimeUnit.DAYS);
        }
    }

}
