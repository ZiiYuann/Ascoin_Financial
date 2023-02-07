package com.tianli.chain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.Service.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.enums.ChainType;
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
import com.tianli.management.query.CoinWithdrawQuery;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.service.FinancialProductService;
import com.tianli.sqs.SqsContext;
import com.tianli.sqs.SqsService;
import com.tianli.sqs.SqsTypeEnum;
import com.tianli.sqs.context.PushAddressContext;
import org.apache.commons.collections4.CollectionUtils;
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
    @Resource
    private FinancialProductService financialProductService;

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
                    .gt(Coin::getStatus, (byte) 0));
            redisTemplate.opsForValue().set(RedisConstants.COIN_PUSH_LIST, coins);
            return coins;
        }
        return (List<Coin>) o;
    }

    @Override
    public List<Coin> pushCoinsWithCache(String name) {
        List<Coin> coins = pushCoinsWithCache();
        return coins.stream().filter(coin -> name.equals(coin.getName()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveOrUpdate(String nickName, CoinIoUQuery query) {
        // 判断是否存在汇率
        currencyService.huobiUsdtRate(query.getName().toLowerCase(Locale.ROOT));
        // 获取小数点位数
        Integer decimals = contractAdapter.getOne(query.getNetwork()).decimals(query.getContract());
        query.setDecimals(decimals);

        coinBaseService.saveOrUpdate(nickName, query);

        // insert
        if (Objects.isNull(query.getId())) {
            Coin coin = managementConverter.toDO(query);
            coin.setCreateBy(nickName);
            coin.setUpdateBy(nickName);
            coinMapper.insert(coin);
            return;
        }


        Coin coin = coinMapper.selectById(query.getId());
        if (coin.getStatus() >= 1) {
            return;
        }
        coin = managementConverter.toDO(query);
        coin.setUpdateBy(nickName);
        coinMapper.updateById(coin);
    }

    @Override
    @Transactional
    public void push(String nickname, CoinStatusQuery query) {
        Long id = query.getId();
        var coin = processStatus(nickname, id);
        // 执行ETH、BSC、TRON 推送数据
        if (ChainType.BSC.equals(coin.getChain()) ||
                ChainType.TRON.equals(coin.getChain()) ||
                ChainType.ETH.equals(coin.getChain())) {
            asyncService.async(() -> this.asyncPush(coin));
        } else {
            successStatus(coin.getId());
            coinBaseService.flushPushListCache();
            this.deletePushListCache();
        }
    }

    @Override
    @Transactional
    public void close(String nickname, CoinStatusQuery query) {
        Long id = query.getId();
        Coin coin = coinMapper.selectById(id);

        List<FinancialProduct> products = financialProductService.list(new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getCoin, coin.getName())
                .eq(FinancialProduct::isDeleted, false));
        if (CollectionUtils.isNotEmpty(products)) {
            ErrorCodeEnum.throwException("该币种已经配置理财产品，无法下架");
        }

        coin.setStatus((byte) 3);
        coinMapper.updateById(coin);
        this.deletePushListCache();

        Integer count = coinMapper.selectCount(new LambdaQueryWrapper<Coin>()
                .eq(Coin::getName, coin.getName())
                .eq(Coin::getStatus, (byte) 2));

        if (Objects.isNull(count) || count == 0) {
            coinBaseService.notShow(coin.getName());
            coinBaseService.deletePushListCache();
        }

    }

    @Override
    public void push(Coin coin) {
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
                    coinBaseService.flushPushListCache();
                    this.deletePushListCache();
                    webHookService.dingTalkSend("新币种注册消费结束");
                    return;
                }
                if (i == 299) {
                    webHookService.dingTalkSend("300s时限内币别未完成注册操作，请手动修改状态");
                }
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            webHookService.dingTalkSend("异步push注册信息结束异常", e);
        }
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

    @Override
    public Coin mainToken(ChainType chain, String name) {
        return this.getOne(new LambdaQueryWrapper<Coin>()
                .eq(Coin::getChain, chain)
                .eq(Coin::getName, name)
                .eq(Coin::isMainToken, true));
    }

    @Override
    @Transactional
    public void withdrawConfig(String nickname, CoinWithdrawQuery query) {
        Coin coin = this.getById(query.getId());
        coin.setUpdateBy(nickname);
        coin.setWithdrawDecimals(query.getWithdrawDecimals());
        coin.setWithdrawMin(query.getWithdrawMin());
        coin.setWithdrawFixedAmount(query.getWithdrawFixedAmount());
        this.updateById(coin);
    }

    @Override
    public void deletePushListCache() {
        redisTemplate.delete(RedisConstants.COIN_PUSH_LIST);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        coinMapper.deleteById(id);
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
    public Coin processStatus(String nickname, Long id) {
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
        coin.setStatus((byte) 1);
        coin.setUpdateBy(nickname);
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
