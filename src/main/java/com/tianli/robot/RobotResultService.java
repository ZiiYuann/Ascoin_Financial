package com.tianli.robot;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.tianli.common.CommonFunction;
import com.tianli.robot.mapper.RobotResult;
import com.tianli.robot.mapper.RobotResultMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


@Service
public class RobotResultService extends ServiceImpl<RobotResultMapper, RobotResult> {
    public void saveList(long id, int auto_count, double win_rate) {
        int win_count = (int) (win_rate * auto_count);
        int lose_count = auto_count - win_count;
        List<RobotResult> list = Lists.newArrayListWithCapacity(auto_count);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (Long index = 1L; index <= auto_count; index++) {
            int res;
            if (win_count > 0 && lose_count > 0) {
                boolean win = random.nextBoolean();
                if (win) {
                    res = 1;
                    win_count--;
                } else {
                    res = 0;
                    lose_count --;
                }
            }else if (win_count > 0) {
                res = 1;
                win_count--;
            } else {
                res = 0;
                lose_count --;
            }
            LocalDateTime now = LocalDateTime.now();
            list.add(RobotResult.builder()
                    .id(CommonFunction.generalId())
                    .create_time(now)
                    .update_time(now)
                    .bet_result(res)
                    .status(0)
                    .bet_index(index)
                    .robot_code(id)
                    .build());

        }
        super.saveBatch(list);
    }

}
