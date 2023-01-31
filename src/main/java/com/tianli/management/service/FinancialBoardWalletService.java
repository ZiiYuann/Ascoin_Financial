package com.tianli.management.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.vo.AccountBalanceSimpleVO;
import com.tianli.address.Service.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.OrderMQuery;
import com.tianli.charge.query.ServiceAmountQuery;
import com.tianli.charge.service.OrderService;
import com.tianli.common.Constants;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.service.CurrencyService;
import com.tianli.financial.query.FinancialRecordQuery;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.fund.query.FundRecordQuery;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.dto.HotWalletBoardDto;
import com.tianli.management.entity.FinancialBoardWallet;
import com.tianli.management.mapper.FinancialBoardWalletMapper;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.vo.BoardAssetsVO;
import com.tianli.management.vo.BoardWalletVO;
import com.tianli.management.vo.FinancialWalletBoardVO;
import com.tianli.tool.time.TimeTool;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Service
public class FinancialBoardWalletService extends ServiceImpl<FinancialBoardWalletMapper, FinancialBoardWallet> {

    @Resource
    private AddressService addressService;
    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private FinancialBoardWalletMapper financialWalletBoardMapper;
    @Resource
    private RedisLock redisLock;
    @Resource
    private OrderService orderService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private IFundRecordService fundRecordService;

    public FinancialBoardWallet getFinancialBoardWallet(LocalDateTime startTime, LocalDateTime entTime, FinancialBoardWallet financialBoardWallet) {
        financialBoardWallet = Optional.ofNullable(financialBoardWallet).orElse(FinancialBoardWallet.getDefault());
        ServiceAmountQuery serviceAmountQuery = new ServiceAmountQuery();
        serviceAmountQuery.setStartTime(startTime);
        serviceAmountQuery.setEndTime(entTime);
        serviceAmountQuery.setChargeType(ChargeType.withdraw);
        BigDecimal rechargeAmount = orderService.amountDollarSumByCompleteTime(ChargeType.recharge, startTime, entTime);
        BigDecimal withdrawAmount = orderService.amountDollarSumByCompleteTime(ChargeType.withdraw, startTime, entTime);

        BigInteger activeWalletCount = addressService.activeCount(startTime, entTime);
        // 暂时只有提币存在手续费
        BigDecimal totalServiceAmount = orderService.serviceAmountDollarSumByCompleteTime(serviceAmountQuery);
        serviceAmountQuery.setCoin("usdt");
        BigDecimal usdtServiceAmount = orderService.serviceAmountDollarSumByCompleteTime(serviceAmountQuery);

        financialBoardWallet.setRechargeAmount(rechargeAmount);
        financialBoardWallet.setWithdrawAmount(withdrawAmount);
        financialBoardWallet.setActiveWalletCount(activeWalletCount);
        financialBoardWallet.setTotalServiceAmount(totalServiceAmount);
        financialBoardWallet.setUsdtServiceAmount(usdtServiceAmount);
        return financialBoardWallet;
    }

    public BoardWalletVO walletBoard(FinancialBoardQuery query) {

        // 按用户输入时间
        FinancialBoardWallet financialBoardWallet = this.getFinancialBoardWallet(query.getStartTime(), query.getEndTime(), null);
        var addressQuery =
                new LambdaQueryWrapper<Address>().between(Address::getCreateTime, query.getStartTime(), query.getEndTime());
        long newActiveWalletCount = addressService.count(addressQuery);
        int totalActiveWalletCount = addressService.count();

        // 本日数据
        LocalDateTime todayBegin = TimeTool.minDay(LocalDateTime.now());
        LocalDateTime todayEnd = todayBegin.plusDays(1);
        FinancialBoardWallet financialBoardWalletToday = getFinancialBoardWallet(todayBegin, todayEnd, null);
        financialBoardWalletToday.setCreateTime(todayBegin.toLocalDate());


        int offsetDay = -13;
        //获取13天前零点时间
        //构建13天的数据
        Map<String, FinancialWalletBoardVO> financialWalletBoardVOMap = new LinkedHashMap<>();
        for (int i = offsetDay; i <= 0; i++) {
            DateTime time = DateUtil.offsetDay(new Date(), i);
            String dateTimeStr = Constants.standardDateFormatter.format(time.toLocalDateTime());
            financialWalletBoardVOMap.put(dateTimeStr, FinancialWalletBoardVO.getDefault(time.toLocalDateTime().toLocalDate()));
        }

        var walletBoardQuery = new LambdaQueryWrapper<FinancialBoardWallet>()
                .between(FinancialBoardWallet::getCreateTime, todayBegin.plusDays(-13), todayBegin);
        List<FinancialBoardWallet> financialBoardWallets13 =
                Optional.ofNullable(financialWalletBoardMapper.selectList(walletBoardQuery)).orElse(new ArrayList<>());
        financialBoardWallets13.add(financialBoardWalletToday);

        financialBoardWallets13.forEach(o -> {
            FinancialWalletBoardVO financialWalletBoardVO = managementConverter.toVO(o);
            String dateTimeStr = Constants.standardDateFormatter.format(financialWalletBoardVO.getCreateTime());
            financialWalletBoardVOMap.put(dateTimeStr, financialWalletBoardVO);
        });

        BoardWalletVO vo = managementConverter.toFinancialWalletBoardSummaryVO(financialBoardWallet);
        vo.setData(new ArrayList<>(financialWalletBoardVOMap.values()));
        vo.setNewActiveWalletCount(BigInteger.valueOf(newActiveWalletCount));
        vo.setTotalActiveWalletCount(BigInteger.valueOf(totalActiveWalletCount));

        return vo;
    }

    /**
     * 获取当日的数据
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FinancialBoardWallet getByDate(LocalDate todayBegin) {
        redisLock.waitLock(RedisLockConstants.FINANCIAL_WALLET_BOARD_GET, 1000);

        LambdaQueryWrapper<FinancialBoardWallet> query =
                new LambdaQueryWrapper<FinancialBoardWallet>().eq(FinancialBoardWallet::getCreateTime, todayBegin);

        FinancialBoardWallet financialBoardWallet = financialWalletBoardMapper.selectOne(query);
        try {
            if (Objects.isNull(financialBoardWallet)) {
                redisLock.lock(RedisLockConstants.FINANCIAL_WALLET_BOARD_GET, 5L, TimeUnit.SECONDS);
                FinancialBoardWallet boardWallet = FinancialBoardWallet.getDefault();
                boardWallet.setCreateTime(todayBegin);
                financialWalletBoardMapper.insert(boardWallet);
                return boardWallet;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            redisLock.unlock(RedisLockConstants.FINANCIAL_WALLET_BOARD_GET);
        }
        return financialBoardWallet;
    }

    /**
     * 缓存热钱包余额数据
     *
     * @param startTime 时间
     * @param endTime   时间
     */
    public HotWalletBoardDto setAssetsBoardCache(LocalDateTime startTime, LocalDateTime endTime) {
        String key = RedisConstants.HOT_WALLET_BALANCE;
        String subKey = RedisConstants.HOT_WALLET_BALANCE + ":" + endTime.toLocalDate().toString();

        OrderMQuery query = new OrderMQuery();
        query.setEndTime(endTime);
        query.setStartTime(startTime);
        query.setType(ChargeType.recharge);
        List<AmountDto> rechargeAmounts = orderService.amount(query);
        query.setType(ChargeType.withdraw);
        List<AmountDto> withdrawAmounts = orderService.amount(query);
        // 所有账户余额
        List<AccountBalanceSimpleVO> accountBalanceSimpleVOS = accountBalanceService.accountBalanceSimpleVOs();
        // 理财持有
        List<AmountDto> financialAmounts = financialRecordService.hold(new FinancialRecordQuery());
        // 基金持有
        List<AmountDto> fundAmounts = fundRecordService.hold(new FundRecordQuery());

        HotWalletBoardDto hotWalletBoardDto = HotWalletBoardDto.builder()
                .accountBalances(accountBalanceSimpleVOS)
                .rechargeAmounts(rechargeAmounts)
                .withdrawAmounts(withdrawAmounts)
                .financialAmounts(financialAmounts)
                .fundAmounts(fundAmounts)
                .build();

        stringRedisTemplate.opsForHash().put(key, subKey, JSONUtil.toJsonStr(hotWalletBoardDto));
        return hotWalletBoardDto;
    }

    /**
     * 获取热钱包缓存数据
     *
     * @param time 时间
     * @return 缓存数据
     */
    public HotWalletBoardDto getAssetsBoardCache(LocalDateTime time) {
        String key = RedisConstants.HOT_WALLET_BALANCE;
        String subKey = RedisConstants.HOT_WALLET_BALANCE + ":" + time.toLocalDate().toString();
        Object o = stringRedisTemplate.opsForHash().get(key, subKey);
        if (Objects.isNull(o)) {
            return null;
        }
        return JSONUtil.toBean((String) o, HotWalletBoardDto.class);
    }

    /**
     * 获取数据展示版
     *
     * @param query 请求参数
     * @return 展版
     */
    public BoardAssetsVO assetsBoard(FinancialBoardQuery query) {

        int offsetDay = -13;
        //构建13天的数据
        Map<String, HotWalletBoardDto> boardMap = new LinkedHashMap<>();
        for (int i = offsetDay; i <= 0; i++) {
            DateTime time = DateUtil.offsetDay(new Date(), i);
            String dateTimeStr = DateUtil.format(time, "yyyy-MM-dd");
            HotWalletBoardDto hotWalletBoardDto = this.getAssetsBoardCache(time.toLocalDateTime());
            boardMap.put(dateTimeStr, hotWalletBoardDto);
        }

        // 当日数据 总资产不支持实时变化
        LocalDateTime now = LocalDateTime.now();
        HotWalletBoardDto hotWalletBoardDto = this.setAssetsBoardCache(now.toLocalDate().atStartOfDay(), now);
        boardMap.put(now.toLocalDate().toString(), hotWalletBoardDto);

        // 查询条件数据
        var queryData = TimeTool.Util.DAY.equals(query.getTimeUtil())
                ? hotWalletBoardDto : this.setAssetsBoardCache(query.getStartTime(), query.getEndTime());

        var data = boardMap.entrySet().stream().map(entry -> {
            HotWalletBoardDto dto = entry.getValue();
            if (Objects.isNull(dto)) {
                return new BoardAssetsVO(entry.getKey());
            }

            return this.getByHotWalletBoardDto(entry.getKey(),dto);

        }).collect(Collectors.toList());

        BoardAssetsVO queryVO = getByHotWalletBoardDto(null, queryData);
        return BoardAssetsVO.builder()
                .data(data)
                .totalAssets(queryVO.getTotalAssets())
                .accrueRechargeFee(queryVO.getAccrueRechargeFee())
                .accrueWithdrawFee(queryVO.getAccrueWithdrawFee())
                .build();
    }

    private BoardAssetsVO getByHotWalletBoardDto(String createTime, HotWalletBoardDto dto) {
        var accountAmounts = dto.getAccountBalances().stream()
                .map(vo -> new AmountDto(vo.getBalanceAmount(), vo.getCoin())).collect(Collectors.toList());
        return BoardAssetsVO.builder()
                .createTime(createTime)
                .accrueWithdrawFee(currencyService.calDollarAmount(dto.getWithdrawAmounts()))
                .accrueRechargeFee(currencyService.calDollarAmount(dto.getRechargeAmounts()))
                .totalAssets(BigDecimal.ZERO
                        .add(currencyService.calDollarAmount(accountAmounts))
                        .add(currencyService.calDollarAmount(dto.getFinancialAmounts()))
                        .add(currencyService.calDollarAmount(dto.getFundAmounts()))
                )
                .build();
    }


}
