package com.tianli.chain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.mapper.CoinMapper;
import com.tianli.chain.service.CoinService;
import com.tianli.common.RedisConstants;
import com.tianli.currency.service.CurrencyService;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinStatusQuery;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-21
 **/
@Service
public class CoinServiceImpl extends ServiceImpl<CoinMapper, Coin> implements CoinService {

    @Resource
    private CoinMapper coinMapper;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public void saveOrUpdate(Long uid, CoinIoUQuery query) {

        // 判断是否存在汇率
        currencyService.huobiUsdtRate(query.getName().toLowerCase(Locale.ROOT));

        // insert
        if (Objects.isNull(query.getId())) {
            Coin coin = managementConverter.toDO(query);
            coin.setCreateBy(uid);
            coin.setUpdateBy(uid);
            coinMapper.insert(coin);
            return;
        }

        Coin coin = coinMapper.selectById(query.getId());
        coin.setLogo(query.getLogo());
        coin.setWeight(query.getWeight());
        coinMapper.updateById(coin);
        // 批量激活
    }

    @Override
    public void flushCache() {
        // 删除缓存
        redisTemplate.delete(RedisConstants.COIN_LIST);

        // 只缓存上架的数据
        List<Coin> coins = coinMapper.selectList(new LambdaQueryWrapper<Coin>()
                .eq(Coin::getStatus, 1)
                .orderByDesc(Coin :: getWeight));
        redisTemplate.opsForValue().set(RedisConstants.COIN_LIST, coins);
    }

    @Override
    @Transactional
    public void status(Long uid, CoinStatusQuery query) {
        Long id = query.getId();
        Coin coin = coinMapper.selectById(id);
        Optional.ofNullable(coin).orElseThrow(NullPointerException::new);

        coin.setCreateBy(uid);
        if (coin.getStatus() == query.getStatus()) {
            return;
        }

        //  下架 或者 重新上架
        if (query.getStatus() == 0 || (query.getStatus() == 1 && coin.isPush())) {
            coin.setStatus(query.getStatus());
            coinMapper.updateById(coin);
            return;
        }


    }

}
