package com.tianli.management.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.entity.WalletImputationLog;
import com.tianli.chain.service.WalletImputationLogService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.service.contract.Web3jContractOperation;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderChargeInfoService;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.service.CurrencyService;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.entity.WithdrawServiceFee;
import com.tianli.management.mapper.WithdrawServiceFeeMapper;
import com.tianli.management.service.WithdrawServiceFeeService;
import com.tianli.management.vo.WithdrawServiceFeeVO;
import lombok.AllArgsConstructor;
import lombok.Data;
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
    private ManagementConverter managementConverter;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private WalletImputationLogService walletImputationLogService;


    @Override
    @Transactional
    public void init(LocalDate startTime, LocalDate endTime) {


        var withdrawServiceFeeDTOs = getWithdrawServiceFeeDTOs(startTime, endTime);
        var imputationServiceFeeDTOs = getImputationServiceFeeDTOs(startTime, endTime);

        withdrawServiceFeeDTOs.addAll(imputationServiceFeeDTOs);


        var serviceFeeMap = withdrawServiceFeeDTOs.stream()
                .collect(Collectors.groupingBy(ServiceFeeDTO::getTime));


        for (Map.Entry<String, List<ServiceFeeDTO>> entry : serviceFeeMap.entrySet()) {
            BigDecimal eth = BigDecimal.ZERO;
            BigDecimal bnb = BigDecimal.ZERO;
            BigDecimal trx = BigDecimal.ZERO;

            LocalDate date = LocalDate.parse(entry.getKey());


            List<ServiceFeeDTO> serviceFeeDTOS = entry.getValue();

            for (ServiceFeeDTO serviceFeeDTO : serviceFeeDTOS) {
                try {
                    Web3jContractOperation web3j = contractAdapter.getWeb3j(serviceFeeDTO.getNetworkType());

                    BigDecimal amount = web3j.getConsumeFee(serviceFeeDTO.getTxid());

                    switch (serviceFeeDTO.getNetworkType()) {
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
//                    webHookService.dingTalkSend("异常提现订单【hash数值异常】：" + order.getOrderNo());
                    log.error("异常提现订单【hash数值异常】：" + serviceFeeDTO.getTxid());
//                    e.printStackTrace();
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


    /**
     * 获取提现手续费信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 提现数据信息
     */
    private List<ServiceFeeDTO> getWithdrawServiceFeeDTOs(LocalDate startTime, LocalDate endTime) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .in(Order::getType, ChargeType.withdraw)
                .in(Order::getStatus, List.of(ChargeStatus.chain_success, ChargeStatus.chain_fail));

        if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
            queryWrapper = queryWrapper.between(Order::getCreateTime, startTime, endTime);
        }

        if (Objects.nonNull(startTime) && Objects.isNull(endTime)) {
            queryWrapper = queryWrapper.ge(Order::getCreateTime, startTime);
        }

        // 提现订单
        List<Order> withdrawOrders = orderService.list(queryWrapper);

        return withdrawOrders.stream().map(order -> {
            OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(order.getRelatedId());
            return new ServiceFeeDTO(orderChargeInfo.getNetwork(), orderChargeInfo.getTxid()
                    , orderChargeInfo.getCreateTime().toLocalDate().toString());
        }).collect(Collectors.toList());
    }


    /**
     * 获取提现归集手续费信息
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 提现数据信息
     */
    private List<ServiceFeeDTO> getImputationServiceFeeDTOs(LocalDate startTime, LocalDate endTime) {
        LambdaQueryWrapper<WalletImputationLog> queryWrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(startTime) && Objects.nonNull(endTime)) {
            queryWrapper = queryWrapper.between(WalletImputationLog::getFinishTime, startTime, endTime);
        }

        if (Objects.nonNull(startTime) && Objects.isNull(endTime)) {
            queryWrapper = queryWrapper.ge(WalletImputationLog::getFinishTime, startTime);
        }

        // 提现订单
        List<WalletImputationLog> walletImputationLogs = walletImputationLogService.list(queryWrapper);

        return walletImputationLogs.stream().map(walletImputationLog ->
                new ServiceFeeDTO(walletImputationLog.getNetwork(), walletImputationLog.getTxid()
                        , walletImputationLog.getFinishTime().toLocalDate().toString())).collect(Collectors.toList());

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

        withdrawServiceFees.forEach(o -> {
            WithdrawServiceFeeVO vo = managementConverter.toWithdrawServiceFeeVO(o);
            vo.setTrxUsdt(currencyService.getDollarRate("trx").multiply(vo.getTrx()));
            vo.setBnbUsdt(currencyService.getDollarRate("bnb").multiply(vo.getBnb()));
            vo.setEthUsdt(currencyService.getDollarRate("eth").multiply(vo.getEth()));
            String dateTimeStr = vo.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            withdrawServiceFeeVOMap.put(dateTimeStr, vo);
        });

        WithdrawServiceFee totalAmount = this.getBaseMapper().getTotalAmount();
        WithdrawServiceFeeVO vo = managementConverter.toWithdrawServiceFeeVO(totalAmount);
        vo.setTrxUsdt(currencyService.getDollarRate("trx").multiply(vo.getTrx()));
        vo.setBnbUsdt(currencyService.getDollarRate("bnb").multiply(vo.getBnb()));
        vo.setEthUsdt(currencyService.getDollarRate("eth").multiply(vo.getEth()));
        vo.setFees(new ArrayList<>(withdrawServiceFeeVOMap.values()));

        return vo;
    }

    @Data
    @AllArgsConstructor
    private class ServiceFeeDTO {
        private NetworkType networkType;
        private String txid;
        private String time;
    }
}
