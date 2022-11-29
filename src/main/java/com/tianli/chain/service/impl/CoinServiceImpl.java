package com.tianli.chain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.mapper.CoinMapper;
import com.tianli.chain.service.CoinService;
import com.tianli.common.RedisConstants;
import com.tianli.common.async.AsyncService;
import com.tianli.common.webhook.WebHookService;
import com.tianli.currency.service.CurrencyService;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinStatusQuery;
import com.tianli.management.query.CoinsQuery;
import com.tianli.management.vo.MCoinListVO;
import com.tianli.sqs.SqsContext;
import com.tianli.sqs.SqsService;
import com.tianli.sqs.SqsTypeEnum;
import com.tianli.sqs.context.PushAddressContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    @Resource
    private AddressService addressService;
    @Resource
    private SqsService sqsService;
    @Resource
    private AsyncService asyncService;
    @Resource
    private WebHookService webHookService;
    // 存储每批数据的临时容器
    private final List<Address> addresses = new ArrayList<>();
    private int size = 0;
    private final int BATCH_SIZE = 50;

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
                .orderByDesc(Coin::getWeight));
        redisTemplate.opsForValue().set(RedisConstants.COIN_LIST, coins);
    }

    @Override
    @Transactional
    public void push(Long uid, CoinStatusQuery query) {
        Long id = query.getId();
        var coin = processStatus(id);
        asyncService.async(() -> this.asyncPush(coin));
    }


    /**
     * 异步push
     *
     * @param coin 币别属性
     */
    private void asyncPush(Coin coin) {
        webHookService.dingTalkSend("正在异步push注册信息，请勿重启服务器");
        try {
            Long maxId = addressService.getBaseMapper().maxId();
            addressService.getBaseMapper().flow(maxId, resultContext -> {
                Address address = resultContext.getResultObject();
                addresses.add(address);
                size++;
                if (size == BATCH_SIZE) {
                    pushSqs(coin);
                }
            });
            pushSqs(coin);
            webHookService.dingTalkSend("异步push注册信息结束");
        } catch (Exception e) {
            webHookService.dingTalkSend("异步push注册信息结束异常", e);
        }
    }

    @Override
    public IPage<MCoinListVO> list(Page<Coin> page, CoinsQuery query) {
        var queryWrapper = new LambdaQueryWrapper<Coin>();

        if (StringUtils.isNotBlank(query.getName())) {
            queryWrapper = queryWrapper.like(Coin::getName, query.getName());
        }

        if (StringUtils.isNotBlank(query.getContract())) {
            queryWrapper = queryWrapper.like(Coin::getContract, query.getContract());
        }

        if (Objects.nonNull(query.getChain())) {
            queryWrapper = queryWrapper.eq(Coin::getChain, query.getChain());
        }

        if (Objects.nonNull(query.getNetwork())) {
            queryWrapper = queryWrapper.eq(Coin::getNetwork, query.getNetwork());
        }

        if (Objects.nonNull(query.getStatus())) {
            queryWrapper = queryWrapper.eq(Coin::getStatus, query.getStatus());
        }

        return this.page(page, queryWrapper).convert(managementConverter::toMCoinListVO);
    }

    private void pushSqs(Coin coin) {
        try {

            List<String> tos = new ArrayList<>();
            switch (coin.getChain()) {
                case ETH:
                    tos = addresses.stream().map(Address::getEth).collect(Collectors.toList());
                    break;
                case BSC:
                    tos = addresses.stream().map(Address::getBsc).collect(Collectors.toList());
                    break;
                case TRON:
                    tos = addresses.stream().map(Address::getTron).collect(Collectors.toList());
                    break;
            }
            PushAddressContext addressContext = new PushAddressContext(tos, coin);
            SqsContext<PushAddressContext> sqsContext = new SqsContext<>(SqsTypeEnum.ADD_COIN_PUSH, addressContext);
            sqsService.send(sqsContext);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            size = 0;
            addresses.clear();
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Coin processStatus(Long id) {
        Coin coin = coinMapper.selectById(id);
        Optional.ofNullable(coin).orElseThrow(NullPointerException::new);

        if (coin.getStatus() > 1) {
            throw new UnsupportedOperationException();
        }

        // 修改状态为 上架中
        // todo 激活需要修改逻辑
        coin.setStatus((byte) 1);
        coinMapper.updateById(coin);
        return coin;
    }

}
