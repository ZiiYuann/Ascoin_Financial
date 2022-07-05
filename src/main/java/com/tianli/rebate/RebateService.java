package com.tianli.rebate;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.rebate.mapper.Rebate;
import com.tianli.rebate.mapper.RebateMapper;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 返佣表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@Service
public class RebateService extends ServiceImpl<RebateMapper, Rebate> {

    public BigInteger totalRebateAmountWithInterval(long uid, LocalDateTime startTime, LocalDateTime endTime){
        return baseMapper.selectTotalRebateAmountWithInterval(uid, startTime, endTime);
    }

    public BigInteger totalRebateAmount(long uid){
        return totalRebateUsdtAmount(uid);
    }

    public BigInteger totalRebateUsdtAmount(long uid){
        return baseMapper.selectTotalRebateUsdtAmount(uid);
    }

    public BigInteger totalRebateBFAmount(long uid){
        return baseMapper.selectTotalRebateBFAmount(uid);
    }

}
