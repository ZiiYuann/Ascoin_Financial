package com.tianli.chain.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.converter.CoinConverter;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.mapper.CoinBaseMapper;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.chain.service.CoinService;
import com.tianli.common.Constants;
import com.tianli.common.RedisConstants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.query.CoinBaseWithdrawQuery;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinWithdrawQuery;
import com.tianli.management.query.CoinsQuery;
import com.tianli.management.vo.CoinBaseVO;
import com.tianli.management.vo.MCoinListVO;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-30
 **/
@Service
public class CoinBaseServiceImpl extends ServiceImpl<CoinBaseMapper, CoinBase> implements CoinBaseService {

    @Resource
    private CoinService coinService;
    @Resource
    private CoinBaseMapper coinBaseMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private CoinConverter coinConverter;

    @Override
    @Transactional
    public CoinBase saveOrUpdate(String nickName, CoinIoUQuery query) {

        CoinBase coinBase = coinBaseMapper.selectByName(query.getName());

        if (Objects.isNull(coinBase)) {
            coinBase = CoinBase.builder()
                    .name(query.getName())
                    .logo(query.getLogo())
                    .weight(query.getWeight())
                    .createBy(nickName)
                    .updateBy(nickName).build();
            coinBaseMapper.insert(coinBase);
            return coinBase;
        }

        coinBase.setUpdateBy(nickName);
        coinBase.setLogo(query.getLogo());
        coinBase.setWeight(query.getWeight());

        coinBaseMapper.update(coinBase, new LambdaQueryWrapper<CoinBase>().eq(CoinBase::getName, coinBase.getName()));
        return coinBase;
    }

    @Override
    public IPage<MCoinListVO> list(Page<Coin> page, CoinsQuery query) {
        return coinBaseMapper.coins(page, query);
    }

    @Override
    public IPage<CoinBaseVO> baseList(Page<CoinBase> page, CoinsQuery query) {
        return this.page(page,new LambdaQueryWrapper<CoinBase>()
                .like(StrUtil.isNotBlank(query.getName()), CoinBase::getName,query.getName()))
                .convert(coinConverter::toCoinBaseVo);
    }

    @Override
    public void updateConfig(CoinBaseWithdrawQuery query) {
        CoinBase coinBase = this.getById(query.getName());
        if (Objects.isNull(coinBase)) ErrorCodeEnum.throwException("币种不存在");
        coinBase.setWithdrawDecimals(query.getWithdrawDecimals());
        coinBase.setWithdrawMin(query.getWithdrawMin());
        this.updateById(coinBase);
    }

    @Override
    public CoinBase getByName(String name) {
        name = name.toLowerCase(Locale.ROOT);
        Object o = redisTemplate.opsForValue().get(RedisConstants.COIN_BASE + name);
        if (Objects.nonNull(o)) {
            return (CoinBase) o;
        }
        CoinBase coinBase = coinBaseMapper.selectByName(name);
        redisTemplate.opsForValue().set(RedisConstants.COIN_BASE + name, coinBase, 7, TimeUnit.DAYS);
        return coinBase;
    }

    @Override
    public void show(String name) {
        coinBaseMapper.displayOpen(name);
    }

    @Override
    public void notShow(String name) {
        coinBaseMapper.displayColse(name);
    }

    @Override
    public void deleteCache(String name) {
        redisTemplate.delete(RedisConstants.COIN_BASE + name);
    }

    @Override
    public List<CoinBase> flushPushListCache() {
        // 删除缓存
        this.deletePushListCache();

        // 只缓存上架的数据
        List<CoinBase> coins = this.list(new LambdaQueryWrapper<CoinBase>()
                .eq(CoinBase::isDisplay, true));

        redisTemplate.opsForValue().set(RedisConstants.COIN_BASE_LIST, coins, 7, TimeUnit.DAYS);
        return coins;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CoinBase> getPushListCache() {
        Object o = redisTemplate.opsForValue().get(RedisConstants.COIN_BASE_LIST);
        if (Objects.isNull(o)) {
            return flushPushListCache();
        }

        return (List<CoinBase>) o;
    }

    @Override
    public Set<String> pushCoinNames() {
        List<CoinBase> coins = getPushListCache();
        return coins.stream()
                .map(coin -> coin.getName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> pushCoinNames(int version) {
        Set<String> pushCoinNames = pushCoinNames();
        List<Coin> coins = coinService.pushCoinsWithCache();
        List<ChainType> chainTypes = Constants.CHAIN_TYPE_VERSION.get(version);
        if (CollectionUtils.isNotEmpty(chainTypes)) {
            List<String> coinNames = coins.stream().filter(coin -> chainTypes.contains(coin.getChain())).map(Coin::getName)
                    .distinct().collect(Collectors.toList());
            pushCoinNames = pushCoinNames.stream().filter(coinNames::contains).collect(Collectors.toSet());
        }
        return pushCoinNames;
    }

    @Override
    public void deletePushListCache() {
        redisTemplate.delete(RedisConstants.COIN_BASE_LIST);
    }

}
