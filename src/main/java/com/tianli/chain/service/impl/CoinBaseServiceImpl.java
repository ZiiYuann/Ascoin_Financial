package com.tianli.chain.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.mapper.CoinBaseMapper;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.common.RedisConstants;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinsQuery;
import com.tianli.management.vo.MCoinListVO;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-30
 **/
@Service
public class CoinBaseServiceImpl extends ServiceImpl<CoinBaseMapper, CoinBase> implements CoinBaseService {

    @Resource
    private CoinBaseMapper coinBaseMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public CoinBase saveOrUpdate(Long uid, CoinIoUQuery query) {

        CoinBase coinBase = coinBaseMapper.selectById(query.getName());

        if (Objects.isNull(coinBase)) {
            coinBase = CoinBase.builder()
                    .name(query.getName())
                    .logo(query.getLogo())
                    .weight(query.getWeight())
                    .createBy(uid)
                    .updateBy(uid).build();
            coinBaseMapper.insert(coinBase);
            return coinBase;
        }

        coinBase.setUpdateBy(uid);
        coinBase.setLogo(query.getLogo());
        coinBase.setWeight(query.getWeight());

        coinBaseMapper.updateById(coinBase);
        return coinBase;
    }

    @Override
    public IPage<MCoinListVO> list(Page<Coin> page, CoinsQuery query) {
        return coinBaseMapper.coins(page, query);
    }

    @Override
    public CoinBase getByName(String name) {
        Object o = redisTemplate.opsForValue().get(RedisConstants.COIN_BASE + name);
        if (Objects.nonNull(o)) {
            return (CoinBase) o;
        }
        CoinBase coinBase = coinBaseMapper.selectById(name);
        redisTemplate.opsForValue().set(RedisConstants.COIN_BASE + name, coinBase);
        return coinBase;
    }

    @Override
    public void show(String name) {
        coinBaseMapper.show(name);
    }

}
