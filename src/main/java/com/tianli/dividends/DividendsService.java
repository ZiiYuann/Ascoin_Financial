package com.tianli.dividends;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.dividends.mapper.Dividends;
import com.tianli.dividends.mapper.DividendsMapper;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

/**
 * <p>
 * 分红表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@Service
public class DividendsService extends ServiceImpl<DividendsMapper, Dividends> {

    public BigInteger sumAmount(Long uid) {
        return baseMapper.sumAmount(uid);
    }
}
