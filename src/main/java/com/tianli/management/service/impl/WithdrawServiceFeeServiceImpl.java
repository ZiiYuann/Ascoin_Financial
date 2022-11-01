package com.tianli.management.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.service.contract.Web3jContractOperation;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderChargeInfoService;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.webhook.WebHookService;
import com.tianli.currency.service.CurrencyService;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.entity.WithdrawServiceFee;
import com.tianli.management.mapper.WithdrawServiceFeeMapper;
import com.tianli.management.service.WithdrawServiceFeeService;
import com.tianli.management.vo.WithdrawServiceFeeVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-27
 **/
@Service
public class WithdrawServiceFeeServiceImpl extends ServiceImpl<WithdrawServiceFeeMapper, WithdrawServiceFee>
        implements WithdrawServiceFeeService {

    @Resource
    private OrderService orderService;
    @Resource
    private OrderChargeInfoService orderChargeInfoService;
    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private WebHookService webHookService;
    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private CurrencyService currencyService;


    @Override
    @Transactional
    public void init(LocalDate startTime, LocalDate endTime) {

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getType, ChargeType.withdraw)
                .in(Order::getStatus, List.of(ChargeStatus.chain_success, ChargeStatus.chain_fail));

        if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
            queryWrapper = queryWrapper.between(Order::getCreateTime, startTime, endTime);
        }

        if (Objects.nonNull(startTime) && Objects.isNull(endTime)) {
            queryWrapper = queryWrapper.ge(Order::getCreateTime, startTime);
        }

        List<Order> withdrawOrders = orderService.list(queryWrapper);
        var orderMap = withdrawOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getCreateTime().toLocalDate().toString()));

        for (Map.Entry<String, List<Order>> entry : orderMap.entrySet()) {
            BigDecimal eth = BigDecimal.ZERO;
            BigDecimal bnb = BigDecimal.ZERO;
            BigDecimal trx = BigDecimal.ZERO;

            LocalDate date = LocalDate.parse(entry.getKey());


            List<Order> orders = entry.getValue();

            for (Order order : orders) {
                OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(order.getRelatedId());
                try {
                    Web3jContractOperation web3j = contractAdapter.getWeb3j(orderChargeInfo.getNetwork());

                    BigDecimal amount = web3j.getConsumeFee(orderChargeInfo.getTxid());

                    switch (orderChargeInfo.getNetwork()) {
                        case bep20:
                            bnb = bnb.add(amount);
                            break;
                        case erc20:
                            eth = eth.add(amount);
                            break;
                        case trc20:
                            trx = trx.add(amount);
                            break;
                        default:
                            break;
                    }
                } catch (Exception e) {
                    webHookService.dingTalkSend("异常提现订单【hash数值异常】：" + order.getOrderNo());
                    e.printStackTrace();
                }

            }

            WithdrawServiceFee withdrawServiceFee = WithdrawServiceFee.builder()
                    .id(CommonFunction.generalId())
                    .bnb(bnb)
                    .eth(eth)
                    .trx(trx)
                    .createTime(date).build();
            this.saveOrUpdate(withdrawServiceFee, new LambdaQueryWrapper<WithdrawServiceFee>()
                    .eq(WithdrawServiceFee::getCreateTime, date));
        }
    }

    @Override
    public void init() {
        this.init(null, null);
    }

    @Override
    public void init(LocalDate startTime) {
        this.init(startTime, null);
    }

    @Override
    public WithdrawServiceFeeVO board() {

        // 更新今日数据
        init(LocalDate.now());

        // 按用户输入时间
        var queryWrapper =
                new LambdaQueryWrapper<WithdrawServiceFee>().between(WithdrawServiceFee::getCreateTime, LocalDate.now().plusDays(-14),
                        LocalDate.now());

        List<WithdrawServiceFee> withdrawServiceFees = this.getBaseMapper().selectList(queryWrapper);


        int offsetDay = -14;
        //获取13天前零点时间
        //构建13天的数据
        Map<String, WithdrawServiceFeeVO> withdrawServiceFeeVOMap = new LinkedHashMap<>();
        for (int i = offsetDay; i <= 0; i++) {
            DateTime time = DateUtil.offsetDay(new Date(), i);
            String dateTimeStr = DateUtil.format(time, "yyyy-MM-dd");
            withdrawServiceFeeVOMap.put(dateTimeStr, WithdrawServiceFeeVO.getDefault(time.toLocalDateTime().toLocalDate()));
        }


        EnumMap<CurrencyCoin, BigDecimal> dollarRateMap = currencyService.getDollarRateMap();


        withdrawServiceFees.forEach(o -> {
            WithdrawServiceFeeVO vo = managementConverter.toWithdrawServiceFeeVO(o);
            vo.setTrxUsdt(dollarRateMap.get(CurrencyCoin.trx).multiply(vo.getTrx()));
            vo.setBnbUsdt(dollarRateMap.get(CurrencyCoin.bnb).multiply(vo.getBnb()));
            vo.setEthUsdt(dollarRateMap.get(CurrencyCoin.eth).multiply(vo.getEth()));
            String dateTimeStr = vo.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            withdrawServiceFeeVOMap.put(dateTimeStr, vo);
        });

        WithdrawServiceFee totalAmount = this.getBaseMapper().getTotalAmount();
        WithdrawServiceFeeVO vo = managementConverter.toWithdrawServiceFeeVO(totalAmount);
        vo.setTrxUsdt(dollarRateMap.get(CurrencyCoin.trx).multiply(vo.getTrx()));
        vo.setBnbUsdt(dollarRateMap.get(CurrencyCoin.bnb).multiply(vo.getBnb()));
        vo.setEthUsdt(dollarRateMap.get(CurrencyCoin.eth).multiply(vo.getEth()));
        vo.setFees(new ArrayList<>(withdrawServiceFeeVOMap.values()));

        return vo;
    }
}
