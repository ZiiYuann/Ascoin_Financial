package com.tianli.chain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.mapper.CoinMapper;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.chain.service.CoinService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.common.RedisConstants;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.webhook.WebHookService;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinStatusQuery;
import com.tianli.sqs.SqsContext;
import com.tianli.sqs.SqsService;
import com.tianli.sqs.SqsTypeEnum;
import com.tianli.sqs.context.PushAddressContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private ContractAdapter contractAdapter;
    // 存储每批数据的临时容器
    private final List<Address> addresses = new ArrayList<>();
    private int size = 0;
    private final int BATCH_SIZE = 50;

    @Override
    @SuppressWarnings("unchecked")
    public List<Coin> pushCoinsWithCache() {
        Object o = redisTemplate.opsForValue().get(RedisConstants.COIN_PUSH_LIST);
        if (Objects.isNull(o)) {
            List<Coin> coins = this.list(new LambdaQueryWrapper<Coin>()
                    .ge(Coin::getStatus, (byte) 0));
            redisTemplate.opsForValue().set(RedisConstants.COIN_PUSH_LIST, coins);
            return coins;
        }
        return (List<Coin>) o;
    }

    @Override
    @Transactional
    public void saveOrUpdate(Long uid, CoinIoUQuery query) {
        // 判断是否存在汇率
        currencyService.huobiUsdtRate(query.getName().toLowerCase(Locale.ROOT));
        // 获取小数点位数
        Integer decimals = contractAdapter.getOne(query.getNetwork()).decimals(query.getContract());
        query.setDecimals(decimals);

        coinBaseService.saveOrUpdate(uid, query);

        // insert
        if (Objects.isNull(query.getId())) {
            Coin coin = managementConverter.toDO(query);
            coin.setCreateBy(uid);
            coin.setUpdateBy(uid);
            coinMapper.insert(coin);
            return;
        }


        Coin coin = coinMapper.selectById(query.getId());
        if (coin.getStatus() >= 1) {
            return;
        }
        coin = managementConverter.toDO(query);
        coin.setUpdateBy(uid);
        coinMapper.updateById(coin);
    }

    @Override
    public List<CoinBase> flushCache() {
        // 删除缓存
        redisTemplate.delete(RedisConstants.COIN_BASE_LIST);
        redisTemplate.delete(RedisConstants.COIN_PUSH_LIST);

        // 只缓存上架的数据
        List<CoinBase> coins = coinBaseService.list(new LambdaQueryWrapper<CoinBase>()
                .eq(CoinBase::isDisplay, true));

        redisTemplate.opsForValue().set(RedisConstants.COIN_BASE_LIST, coins);
        return coins;
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

            for (int i = 0; i < 300; i++) {
                if (sqsService.receiveAndDelete(null, 5) == 0) {
                    successStatus(coin.getId());
                    flushCache();
                    return;
                }
            }
        } catch (Exception e) {
            webHookService.dingTalkSend("异步push注册信息结束异常", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CoinBase> effectiveCoinsWithCache() {
        Object o = redisTemplate.opsForValue().get(RedisConstants.COIN_BASE_LIST);
        if (Objects.isNull(o)) {
            return flushCache();
        }

        return (List<CoinBase>) o;
    }

    @Override
    public Set<String> effectiveCoinNames() {
        List<CoinBase> coins = effectiveCoinsWithCache();
        return coins.stream()
                .map(coin -> coin.getName().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    @Override
    public Coin getByNameAndNetwork(String name, NetworkType networkType) {
        return this.getOne(new LambdaQueryWrapper<Coin>()
                .eq(Coin::getName, name)
                .eq(Coin::getNetwork, networkType));
    }

    @Override
    public Coin getByContract(String contract) {
        return this.getOne(new LambdaQueryWrapper<Coin>()
                .eq(Coin::getContract, contract));
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
        if (coin.getWithdrawFixedAmount().compareTo(BigDecimal.ZERO) == 0
                || coin.getWithdrawMin().compareTo(BigDecimal.ZERO) == 0) {
            ErrorCodeEnum.COIN_NOT_CONFIG_NOT_EXIST.throwException();
        }

        // 修改状态为 上架中
        // todo 激活需要修改逻辑
        coin.setStatus((byte) 1);
        coinMapper.updateById(coin);
        return coin;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Coin successStatus(Long id) {
        Coin coin = coinMapper.selectById(id);
        Optional.ofNullable(coin).orElseThrow(NullPointerException::new);

        if (coin.getStatus() != 1) {
            throw new UnsupportedOperationException();
        }
        coin.setStatus((byte) 2);
        coinMapper.updateById(coin);

        // 修改为可显示
        coinBaseService.show(coin.getName());
        return coin;
    }

}
