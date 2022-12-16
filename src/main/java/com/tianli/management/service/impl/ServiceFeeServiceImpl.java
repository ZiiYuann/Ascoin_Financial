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
import com.tianli.common.Constants;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.service.CurrencyService;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.entity.ServiceFee;
import com.tianli.management.mapper.ServiceFeeMapper;
import com.tianli.management.query.TimeQuery;
import com.tianli.management.service.ServiceFeeService;
import com.tianli.management.vo.ServiceFeeVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class ServiceFeeServiceImpl extends ServiceImpl<ServiceFeeMapper, ServiceFee>
        implements ServiceFeeService {

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


        var serviceFeeDTOs = new ArrayList<ServiceFeeDTO>();
        serviceFeeDTOs.addAll(getWithdrawServiceFeeDTOs(startTime, endTime));
        serviceFeeDTOs.addAll(getImputationServiceFeeDTOs(startTime, endTime));

        // 根据时间 + 币别 + type进行分类
        Map<String, List<ServiceFeeDTO>> feeByTime =
                serviceFeeDTOs.stream().collect(Collectors.groupingBy(serviceFeeDTO -> serviceFeeDTO.getTime() + serviceFeeDTO.getCoin() + serviceFeeDTO.getType()));


        var newServiceFeeDTOs = feeByTime.values().stream().map(value -> {
            ServiceFeeDTO serviceFeeDTO = value.get(0);
            List<String> txids = value.stream().map(ServiceFeeDTO::getTxid).collect(Collectors.toList());

            ServiceFeeDTO newServiceFeeDTO = new ServiceFeeDTO();
            newServiceFeeDTO.setTime(serviceFeeDTO.getTime());
            newServiceFeeDTO.setCoin(serviceFeeDTO.getCoin());
            newServiceFeeDTO.setType(serviceFeeDTO.getType());
            newServiceFeeDTO.setNetworkType(serviceFeeDTO.getNetworkType());


            BigDecimal serviceAmount = txids.stream().map(txid -> {
                Web3jContractOperation web3j = contractAdapter.getWeb3j(serviceFeeDTO.getNetworkType());
                BigDecimal amount = BigDecimal.ZERO;
                try {
                    amount = web3j.getConsumeFee(serviceFeeDTO.getTxid());
                } catch (Exception e) {
                    log.error("异常提现订单【hash数值异常】：" + serviceFeeDTO.getNetworkType() + " " + txid);
                }
                return amount;
            }).reduce(BigDecimal.ZERO, BigDecimal::add);

            newServiceFeeDTO.setAmount(serviceAmount);
            return newServiceFeeDTO;
        }).collect(Collectors.toList());


        newServiceFeeDTOs.forEach(newServiceFeeDTO -> {
            ServiceFee serviceFee = ServiceFee.builder()
                    .id(CommonFunction.generalId())
                    .amount(newServiceFeeDTO.getAmount())
                    .coin(newServiceFeeDTO.getCoin())
                    .type(newServiceFeeDTO.getType())
                    .createTime(LocalDate.parse(newServiceFeeDTO.getTime(), Constants.standardDateFormatter))
                    .network(newServiceFeeDTO.networkType)
                    .build();

            this.getBaseMapper().delete(new LambdaQueryWrapper<ServiceFee>()
                    .eq(ServiceFee::getCreateTime, serviceFee.getCreateTime())
                    .eq(ServiceFee::getCoin, serviceFee.getCoin())
                    .eq(ServiceFee::getType, serviceFee.getType()));

            this.getBaseMapper().insert(serviceFee);
        });
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
                    , orderChargeInfo.getCreateTime().toLocalDate().toString(), (byte) 0);
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
                        , walletImputationLog.getFinishTime().toLocalDate().toString(), (byte) 1)).collect(Collectors.toList());

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
    public ServiceFeeVO board(TimeQuery timeQuery, byte type) {
        timeQuery.calTime();
        List<ServiceFee> allFeeByType = this.getBaseMapper().getTotalAmount(timeQuery, type);
        List<ServiceFeeVO> allSummaryFee = allFeeByType.stream().map(serviceFee -> {
            ServiceFeeVO serviceFeeVO = managementConverter.toServiceFeeVO(serviceFee);
            serviceFeeVO.setRate(currencyService.getDollarRate(serviceFee.getCoin()));
            serviceFeeVO.setChainType(serviceFee.getNetwork().getChainType());
            return serviceFeeVO;
        }).collect(Collectors.toList());

        BigDecimal allFeeDollar = allSummaryFee.stream()
                .map(serviceFeeVO -> serviceFeeVO.getRate().multiply(serviceFeeVO.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);


        // 更新今日数据
        init(LocalDate.now());

        // 按用户输入时间
        var queryWrapper =
                new LambdaQueryWrapper<ServiceFee>().between(ServiceFee::getCreateTime, LocalDate.now().plusDays(-14),
                                LocalDate.now())
                        .eq(ServiceFee::getType, type);

        List<ServiceFee> serviceFees = this.getBaseMapper().selectList(queryWrapper);

        var serviceFeeMap = serviceFees.stream()
                .collect(Collectors.groupingBy(serviceFee -> serviceFee.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));

        int offsetDay = -14;
        //获取13天前零点时间
        //构建13天的数据
        Map<String, ServiceFeeVO> withdrawServiceFeeVOMap = new LinkedHashMap<>();
        for (int i = offsetDay; i <= 0; i++) {
            DateTime time = DateUtil.offsetDay(new Date(), i);
            String dateTimeStr = DateUtil.format(time, "yyyy-MM-dd");
            withdrawServiceFeeVOMap.put(dateTimeStr, ServiceFeeVO.getDefault(time.toLocalDateTime().toLocalDate()));
        }
        serviceFeeMap.forEach((key, fees) -> {
            LocalDate time = LocalDate.parse(key);
            List<ServiceFeeVO> insideFeeVOs = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (ServiceFee fee : fees) {
                ServiceFeeVO insideVo = managementConverter.toServiceFeeVO(fee);
                BigDecimal dollarRate = currencyService.getDollarRate(fee.getCoin());
                insideVo.setRate(dollarRate);
                BigDecimal amount = dollarRate.multiply(fee.getAmount());
                totalAmount = totalAmount.add(amount);
                insideFeeVOs.add(insideVo);
            }

            ServiceFeeVO externalVo = ServiceFeeVO.builder()
                    .createTime(time)
                    .amount(totalAmount)
                    .fees(insideFeeVOs)
                    .build();

            withdrawServiceFeeVOMap.put(key, externalVo);
        });

        ServiceFeeVO result = new ServiceFeeVO();
        result.setAmount(allFeeDollar);
        result.setFees(new ArrayList<>(withdrawServiceFeeVOMap.values()));
        result.setSummaryFees(allSummaryFee);
        return result;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private class ServiceFeeDTO {
        private NetworkType networkType;
        private String txid;
        private String time;
        private byte type;
        private String coin;

        private BigDecimal amount;


        public ServiceFeeDTO(NetworkType networkType, String txid, String time, byte type) {
            this.networkType = networkType;
            this.txid = txid;
            this.time = time;
            this.type = type;
        }

        public String getCoin() {
            return networkType.getChainType().getMainToken();
        }
    }
}
