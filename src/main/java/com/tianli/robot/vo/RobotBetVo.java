package com.tianli.robot.vo;

import cn.hutool.core.convert.Convert;
import lombok.Builder;
import lombok.Data;

/**
 * @author lzy
 * @date 2022/4/8 4:31 下午
 */
@Builder
@Data
public class RobotBetVo {

    private String id;

    private String time;

    public static RobotBetVo getRobotBetVo(Long id, Integer result) {
        String time = Convert.toStr(System.currentTimeMillis());
        time = time.substring(0, time.length() - 1) + result;
        return RobotBetVo.builder().id(Convert.toStr(id)).time(time).build();
    }
}
