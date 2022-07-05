package com.tianli.robot;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.robot.mapper.RobotCoupon;
import com.tianli.robot.mapper.RobotCouponMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class RobotCouponService extends ServiceImpl<RobotCouponMapper, RobotCoupon> {

    public RobotCoupon getByCode(String code) {
        return super.getOne(Wrappers.lambdaQuery(RobotCoupon.class)
                .eq(RobotCoupon::getActivation_code, code));
    }

    public void incrementUsedCount(long couponId) {
        long update = baseMapper.incrementUsedCount(couponId, LocalDateTime.now());
        if (update <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }
}
