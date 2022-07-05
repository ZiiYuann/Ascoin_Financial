package com.tianli.currency.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.CommonFunction;
import com.tianli.common.Constants;
import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.*;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.mapper.Currency;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.ApplicationContextTool;
import com.tianli.tool.time.TimeTool;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mountcloud.graphql.GraphqlClient;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CurrencyInterestTask {
    public static final String BSC_BLOCK_COUNT = "bsc_block_count";

    public static final String ETH_BLOCK_COUNT = "eth_block_count";

    @Resource
    private CurrencyService currencyService;

    @Resource
    private Gson gson;

    @Resource
    private ConfigService configService;

    @Resource
    private ChargeService chargeService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private GraphService graphService;

    @Resource
    private AddressService addressService;

    @Resource
    private RedisLock redisLock;

    @Resource
    private CurrencyInterestTask currencyInterestTask;

    @Resource
    private AsyncService asyncService;

    private static AtomicInteger threadId = new AtomicInteger(1);

    private static ConcurrentHashMap<String, AtomicInteger> FAIL_COUNT_CACHE = new ConcurrentHashMap<>();

    private final static ScheduledThreadPoolExecutor CURRENCY_INTEREST_TASK_SCHEDULE_EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("CurrencyInterestTask#currencyInterestStat-" + threadId.getAndIncrement());
                return thread;
            }
    );


    /**
     * 每日计算利息task
     */
//    @Scheduled(cron = "0 0 0 1/1 * ? ")
    public void currencyInterestStat() {
        asyncService.async(() -> {
            LocalDateTime now = LocalDateTime.now();
            String day = String.format("%s_%s", now.getMonthValue(), now.getDayOfMonth());
            String redisKey = "CurrencyInterestTask#currencyInterestStat:pageFlag:" + day;
            BoundValueOperations<String, Object> operation = redisTemplate.boundValueOps(redisKey);
            operation.setIfAbsent(0, 1, TimeUnit.HOURS);
            while (true) {
                Long page = operation.increment();
                if (page == null) {
                    break;
                }
                Page<Currency> currencyPage = currencyService.page(new Page<>(page, 20), new LambdaQueryWrapper<Currency>()
                        .eq(Currency::getType, CurrencyTypeEnum.normal)
                );
                List<Currency> records = currencyPage.getRecords();
                if ((records.size()) <= 0) {
                    break;
                }
                String user_balance_daily_rate = configService._get(ConfigConstants.USER_BALANCE_DAILY_RATE);
                if (StringUtils.isBlank(user_balance_daily_rate)) {
                    break;
                }
                double rate;
                try {
                    rate = Double.valueOf(user_balance_daily_rate);
                } catch (NumberFormatException e) {
                    log.warn("\n用户余额日利率解析失败", e);
                    return;
                }
                if(rate <= 0){
                    return;
                }
                for (Currency c : records) {
                    interestStat(c, rate);
                }
            }
        });
    }

    /**
     * 统计每日利息
     */
    private void interestStat(Currency currency, double rate) {
        try {
            // 1. 计算利息
            BigDecimal bigDecimal = new BigDecimal(currency.getRemain());
            // 真是的今日利息数额
            BigDecimal dayInterest = bigDecimal.multiply(new BigDecimal(String.valueOf(rate)));
            BigInteger dayInterestBigInteger = dayInterest.toBigInteger();
            if(dayInterestBigInteger.compareTo(BigInteger.ZERO) <= 0){
                return;
            }
            // 2. 更新余额
            currencyService.increase(currency.getUid(), currency.getType(), dayInterestBigInteger, TimeTool.getDateTimeDisplayString(LocalDateTime.now()), CurrencyLogDes.利息.name());
        } catch (Exception e) {
            String toJson = gson.toJson(currency);
            log.warn("统计每日利息异常: currency:{}, rate:{}", toJson, rate, e);
            CURRENCY_INTEREST_TASK_SCHEDULE_EXECUTOR.schedule(() -> {
                AtomicInteger atomicInteger = FAIL_COUNT_CACHE.get(String.valueOf(currency.getId()));
                if (Objects.isNull(atomicInteger)) {
                    atomicInteger = new AtomicInteger(3);
                    FAIL_COUNT_CACHE.put(String.valueOf(currency.getId()), atomicInteger);
                }
                int andDecrement = atomicInteger.getAndDecrement();
                if (andDecrement > 0) {
                    interestStat(currency, rate);
                } else {
                    log.error("统计每日利息失败: currency:{}, rate:{}", toJson, rate);
                }
            }, 30, TimeUnit.MINUTES);
        }
    }

//    @Scheduled(fixedDelay = 500)
//    public void updateUserBSCBalance() {
//        asyncService.async(() -> {
//            boolean lock = redisLock._lock("CurrencyInterestTask:updateUserBSCBalance", 3L, TimeUnit.MINUTES);
//            if (!lock) {
//                return;
//            }
//            try {
//                currencyInterestTask.commonScan(BSC_BLOCK_COUNT, ApplicationContextTool.getBean("graphqlClient", GraphqlClient.class));
//            } catch (Exception e) {
//                log.error("updateUserBalance Exception:", e);
//            } finally {
//                redisLock.unlock("CurrencyInterestTask:updateUserBSCBalance");
//            }
//        });
//    }

//    @Scheduled(fixedDelay = 500)
//    public void updateUserETHBalance() {
//        asyncService.async(() -> {
//            boolean lock = redisLock._lock("CurrencyInterestTask:updateUserETHBalance", 3L, TimeUnit.MINUTES);
//            if (!lock) {
//                return;
//            }
//            try {
//                currencyInterestTask.commonScan(ETH_BLOCK_COUNT, ApplicationContextTool.getBean("ethGraphqlClient", GraphqlClient.class));
//            } catch (Exception e) {
//                log.error("updateUserBalance Exception:", e);
//            } finally {
//                redisLock.unlock("CurrencyInterestTask:updateUserETHBalance");
//            }
//        });
//    }

    @Transactional(rollbackFor = Exception.class)
    public void commonScan(String blockCount, GraphqlClient graphqlClient) throws IOException {
        String block = configService.get(blockCount);
        long start = Long.parseLong(block);
        Long graphLastBlock = graphService.getGraphLastBlock(graphqlClient);
        if (graphLastBlock < start) {
            return;
        }

        List<Address> list = addressService.list(new LambdaQueryWrapper<Address>()
                .select(Address::getBsc,Address::getEth)
                .isNotNull(blockCount.equals(BSC_BLOCK_COUNT),Address::getBsc)
                .isNotNull(blockCount.equals(ETH_BLOCK_COUNT),Address::getEth)
                .eq(Address::getType, CurrencyTypeEnum.normal));
        List<String> addressList = list.stream().map(address -> {
            if (blockCount.equals(BSC_BLOCK_COUNT)) {
                return address.getBsc().toLowerCase();
            } else if (blockCount.equals(ETH_BLOCK_COUNT)) {
                return address.getEth().toLowerCase();
            }
            return null;
        }).collect(Collectors.toList());

        List<TransferGraphVO> transferGraphVOS = graphService.getTransfer(graphqlClient, start, start + 1, addressList);
        List<RechargeTransferDTO> rechargeTransferDTOList = Lists.newArrayList();
        transferGraphVOS.forEach(transferGraphVO -> {
            RechargeTransferDTO vo = RechargeTransferDTO.trans(transferGraphVO);
            Address address;
            CurrencyTokenEnum token;
            if (blockCount.equals(BSC_BLOCK_COUNT)) {
                address = addressService.getByBsc(transferGraphVO.getTo());
                Long uid = address.getUid();
                if ((token = CurrencyTokenEnum.getToken(transferGraphVO.getCoinAddress().toLowerCase())) != null) {
                    BigInteger value = transferGraphVO.getValue();
                    if(CurrencyTokenEnum.usdt_bep20.equals(token)) {
                        vo.setCurrency_type(TokenCurrencyType.usdt_bep20);
                        value = value.divide(ChargeService.TEN_BILLION);
                    }else if(CurrencyTokenEnum.usdc_bep20.equals(token)) {
                        vo.setCurrency_type(TokenCurrencyType.usdc_bep20);
                        value = value.divide(ChargeService.TEN_BILLION);
                    }else{
                        vo.setCurrency_type(TokenCurrencyType.BF_bep20);
                    }
                    currencyService.increase(uid, CurrencyTypeEnum.normal, token, value, transferGraphVO.getId(), CurrencyLogDes.充值.name());
                }
                vo.setToken(token);
                vo.setUid(uid);
//                vo.setFee(transferGraphVO.getFee());
                vo.setFeeType(TokenCurrencyType.bnb);
            } else if (blockCount.equals(ETH_BLOCK_COUNT)) {
                address = addressService.getByEth(transferGraphVO.getTo());
                Long uid = address.getUid();
                if ((token = CurrencyTokenEnum.getToken(transferGraphVO.getCoinAddress().toLowerCase())) != null) {
                    if(CurrencyTokenEnum.usdt_erc20.equals(token)) {
                        vo.setCurrency_type(TokenCurrencyType.usdt_erc20);
                        vo.setToken(token);
                    }else if(CurrencyTokenEnum.usdc_erc20.equals(token)) {
                        vo.setCurrency_type(TokenCurrencyType.usdc_erc20);
                        vo.setToken(token);
                    }
                    currencyService.increase(uid, CurrencyTypeEnum.normal, token, transferGraphVO.getValue().multiply(ChargeService.ONE_HUNDRED), transferGraphVO.getId(), CurrencyLogDes.充值.name());
                }
                vo.setUid(uid);
                //                vo.setFee(transferGraphVO.getFee());
                vo.setFeeType(TokenCurrencyType.eth);
            }
            rechargeTransferDTOList.add(vo);
        });

        boolean cas = configService.cas(blockCount, block, String.valueOf(Long.parseLong(block) + 1));
        if (!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        if(!rechargeTransferDTOList.isEmpty()){
            CompletableFuture.runAsync(() -> {
                List<Long> uidList = rechargeTransferDTOList.stream().map(RechargeTransferDTO::getUid).collect(Collectors.toList());
                List<UserInfo> userInfos = userInfoService.listByIds(uidList);
                Map<Long, UserInfo> userMap = userInfos.stream().collect(Collectors.toMap(UserInfo::getId, Function.identity()));
                try {
                    chargeService.saveBatch(rechargeTransferDTOList.stream().map(e -> {
                        return Charge.builder()
                                .id(CommonFunction.generalId())
                                .create_time(LocalDateTime.now())
                                .complete_time(LocalDateTime.now())
                                .status(ChargeStatus.chain_success)
                                .uid(e.getUid())
                                .uid_username(userMap.get(e.getUid()).getUsername())
                                .uid_nick(userMap.get(e.getUid()).getNick())
                                .sn(e.getTxid())
                                .currency_type(e.getCurrency_type())
                                .charge_type(ChargeType.recharge)
                                .amount(e.getValue())
                                .fee(BigInteger.ZERO)
                                .real_amount(e.getValue())
                                .from_address(e.getFrom())
                                .to_address(e.getTo())
                                .txid(e.getTxid())
                                .token(e.getToken())
                                .build();
                    }).collect(Collectors.toList()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
        }
    }
}
