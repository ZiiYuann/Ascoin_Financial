package com.tianli.management.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.entity.WalletImputationLog;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.service.WalletImputationLogService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.service.contract.ContractOperation;
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
import com.tianli.management.dto.AmountDto;
import com.tianli.management.entity.ServiceFee;
import com.tianli.management.mapper.ServiceFeeMapper;
import com.tianli.management.query.TimeQuery;
import com.tianli.management.service.ServiceFeeService;
import com.tianli.management.vo.BoardServiceFeeVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public void init(LocalDate startTime, LocalDate endTime, Byte type) {


        var serviceFeeDTOs = new ArrayList<ServiceFeeDTO>();
        if (Objects.isNull(type)) {
            serviceFeeDTOs.addAll(getWithdrawServiceFeeDTOs(startTime, endTime));
            serviceFeeDTOs.addAll(getImputationServiceFeeDTOs(startTime, endTime));
        }
        if (Objects.nonNull(type) && type == 0) {
            serviceFeeDTOs.addAll(getWithdrawServiceFeeDTOs(startTime, endTime));
        }
        if (Objects.nonNull(type) && type == 1) {
            serviceFeeDTOs.addAll(getImputationServiceFeeDTOs(startTime, endTime));
        }

        // 根据时间 + 币别 + type进行分类
        Map<String, List<ServiceFeeDTO>> feeByTime =
                serviceFeeDTOs.stream().collect(Collectors.groupingBy(serviceFeeDTO -> serviceFeeDTO.getTime() + serviceFeeDTO.getCoin() + serviceFeeDTO.getNetworkType() + serviceFeeDTO.getType()));


        var newServiceFeeDTOs = feeByTime.values().stream().map(value -> {
            ServiceFeeDTO serviceFeeDTO = value.get(0);
            List<String> txids = value.stream().map(ServiceFeeDTO::getTxid).collect(Collectors.toList());

            ServiceFeeDTO newServiceFeeDTO = new ServiceFeeDTO();
            newServiceFeeDTO.setTime(serviceFeeDTO.getTime());
            newServiceFeeDTO.setCoin(serviceFeeDTO.getCoin());
            newServiceFeeDTO.setType(serviceFeeDTO.getType());
            newServiceFeeDTO.setNetworkType(serviceFeeDTO.getNetworkType());


            BigDecimal serviceAmount = txids.stream().map(txid -> {
                ContractOperation web3j = contractAdapter.getOne(serviceFeeDTO.getNetworkType());
                BigDecimal amount = BigDecimal.ZERO;
                try {
                    amount = web3j.getConsumeFee(txid);
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

            LambdaQueryWrapper<ServiceFee> queryWrapper = new LambdaQueryWrapper<ServiceFee>()
                    .eq(ServiceFee::getCreateTime, serviceFee.getCreateTime())
                    .eq(ServiceFee::getCoin, serviceFee.getCoin())
                    .eq(ServiceFee::getNetwork, serviceFee.getNetwork())
                    .eq(ServiceFee::getType, serviceFee.getType());

            this.saveOrUpdate(serviceFee, queryWrapper);
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
            queryWrapper = queryWrapper.between(Order::getCompleteTime, startTime, endTime);
        }

        if (Objects.nonNull(startTime) && Objects.isNull(endTime)) {
            queryWrapper = queryWrapper.ge(Order::getCompleteTime, startTime);
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
            queryWrapper = queryWrapper.between(WalletImputationLog::getCreateTime, startTime, endTime);
        }

        if (Objects.nonNull(startTime) && Objects.isNull(endTime)) {
            queryWrapper = queryWrapper.ge(WalletImputationLog::getCreateTime, startTime);
        }

        // 提现订单
        List<WalletImputationLog> walletImputationLogs = walletImputationLogService.list(queryWrapper);

        return walletImputationLogs.stream().map(walletImputationLog ->
                new ServiceFeeDTO(walletImputationLog.getNetwork(), walletImputationLog.getTxid()
                        , walletImputationLog.getCreateTime().toLocalDate().toString(), (byte) 1)).collect(Collectors.toList());

    }

    @Override
    public void init() {
        this.init(null, null, null);
    }

    @Override
    public void init(LocalDate startTime, Byte type) {
        this.init(startTime, null, type);
    }

    @Override
    public BoardServiceFeeVO board(TimeQuery timeQuery, Byte type) {
        // 更新今日数据
        init(LocalDate.now(), type);

        timeQuery.calTime();
        List<ServiceFee> allFeeByType = this.getBaseMapper().getTotalAmount(timeQuery, type);

        HashMap<ChainType, BoardServiceFeeVO> defaultChainMap = BoardServiceFeeVO.getDefaultChainMap();
        allFeeByType.forEach(serviceFee -> {
            BoardServiceFeeVO boardServiceFeeVO = managementConverter.toServiceFeeVO(serviceFee);
            boardServiceFeeVO.setRate(currencyService.getDollarRate(serviceFee.getCoin()));
            boardServiceFeeVO.setChainType(serviceFee.getNetwork().getChainType());
            defaultChainMap.put(serviceFee.getNetwork().getChainType(), boardServiceFeeVO);
        });
        var allSummaryFee = defaultChainMap.values();
        BigDecimal allFeeDollar = defaultChainMap.values().stream()
                .map(serviceFeeVO -> serviceFeeVO.getRate().multiply(serviceFeeVO.getAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
        Map<String, BoardServiceFeeVO> withdrawServiceFeeVOMap = new LinkedHashMap<>();
        for (int i = offsetDay; i <= 0; i++) {
            DateTime time = DateUtil.offsetDay(new Date(), i);
            String dateTimeStr = DateUtil.format(time, "yyyy-MM-dd");
            withdrawServiceFeeVOMap.put(dateTimeStr, BoardServiceFeeVO.getDefault(time.toLocalDateTime().toLocalDate()));
        }
        serviceFeeMap.forEach((key, fees) -> {
            LocalDate time = LocalDate.parse(key);
            List<BoardServiceFeeVO> insideFeeVOs = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (ServiceFee fee : fees) {
                BoardServiceFeeVO insideVo = managementConverter.toServiceFeeVO(fee);
                BigDecimal dollarRate = currencyService.getDollarRate(fee.getCoin());
                insideVo.setRate(dollarRate);
                insideVo.setChainType(fee.getNetwork().getChainType());
                BigDecimal amount = dollarRate.multiply(fee.getAmount());
                totalAmount = totalAmount.add(amount);
                insideFeeVOs.add(insideVo);
            }

            BoardServiceFeeVO externalVo = BoardServiceFeeVO.builder()
                    .createTime(time)
                    .amount(totalAmount)
                    .fees(insideFeeVOs)
                    .build();

            withdrawServiceFeeVOMap.put(key, externalVo);
        });

        BoardServiceFeeVO result = new BoardServiceFeeVO();
        result.setAmount(allFeeDollar);
        result.setFees(new ArrayList<>(withdrawServiceFeeVOMap.values()));
        result.setSummaryFees(new ArrayList<>(allSummaryFee));
        return result;
    }

    @Override
    public BigDecimal serviceFee(Byte type, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<ServiceFee> queryWrapper = new LambdaQueryWrapper<>();

        Optional.ofNullable(type).ifPresent(v -> queryWrapper.eq(ServiceFee::getType, v));
        Optional.ofNullable(startTime).ifPresent(v -> queryWrapper.ge(ServiceFee::getCreateTime, v.toLocalDate()));
        Optional.ofNullable(endTime).ifPresent(v -> queryWrapper.le(ServiceFee::getCreateTime, v.toLocalDate()));


        List<ServiceFee> fees = this.list(queryWrapper);

        List<AmountDto> amountDtos = fees.stream().collect(Collectors.groupingBy(ServiceFee::getCoin))
                .entrySet().stream().map(entry -> {
                    String coin = entry.getKey();
                    List<ServiceFee> value = entry.getValue();

                    BigDecimal amount = value.stream().map(ServiceFee::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

                    return new AmountDto(amount, coin);
                }).collect(Collectors.toList());

        return currencyService.calDollarAmount(amountDtos);
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
