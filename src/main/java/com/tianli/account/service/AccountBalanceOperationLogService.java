package com.tianli.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.entity.AccountBalanceOperationLog;
import com.tianli.account.enums.AccountOperationType;
import com.tianli.account.mapper.AccountBalanceOperationLogMapper;
import com.tianli.account.query.BalanceOperationChargeTypeQuery;
import com.tianli.account.vo.WalletChargeFlowVo;
import com.tianli.charge.entity.OrderChargeType;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.enums.ChargeTypeGroupEnum;
import com.tianli.charge.enums.OperationTypeEnum;
import com.tianli.charge.mapper.OrderChargeTypeMapper;
import com.tianli.charge.service.IOrderChargeTypeService;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.management.query.WalletChargeFlowQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 余额变动记录表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Service
public class AccountBalanceOperationLogService extends ServiceImpl<AccountBalanceOperationLogMapper, AccountBalanceOperationLog> {

    @Resource
    private AccountBalanceOperationLogMapper accountBalanceOperationLogMapper;
    @Resource
    private IOrderChargeTypeService orderChargeTypeService;

    /**
     * 添加余额操作日志
     */
    public AccountBalanceOperationLog save(AccountBalance accountBalance, ChargeType type, String coin, NetworkType networkType
            , BigDecimal amount, String sn, AccountOperationType accountOperationType) {
        AccountBalanceOperationLog currencyLog = AccountBalanceOperationLog.builder()
                .id(CommonFunction.generalId())
                .uid(accountBalance.getUid())
                .logType(accountOperationType)
                .accountBalanceId(accountBalance.getId())
                .coin(coin)
                .network(networkType)
                .chargeType(type)
                .orderNo(sn)
                .des(type.getNameZn())
                .balance(accountBalance.getBalance())
                .freeze(accountBalance.getFreeze())
                .remain(accountBalance.getRemain())
                .amount(amount)
                .createTime(LocalDateTime.now())
                .build();
        accountBalanceOperationLogMapper.insert(currencyLog);
        return currencyLog;
    }


    public IPage<WalletChargeFlowVo> capitalFlowList(PageQuery<AccountBalanceOperationLog> pageQuery
            , WalletChargeFlowQuery query) {
        BalanceOperationChargeTypeQuery balanceOperationChargeTypeQuery =
                ChargeType.balanceOperationChargeTypeQuery(query.getType());

        if (Objects.nonNull(balanceOperationChargeTypeQuery)) {
            query.setType(balanceOperationChargeTypeQuery.getChargeType());
            query.setAccountOperationType(balanceOperationChargeTypeQuery.getAccountOperationType());
        }

        List<OrderChargeType> orderChargeTypes = orderChargeTypeService.list();
        var orderChargeTypeMap = orderChargeTypes.stream().collect(Collectors.toMap(OrderChargeType::getType, o -> o));

        if (Objects.nonNull(query.getOperationGroup())) {
            orderChargeTypes = orderChargeTypes.stream()
                    .filter(o -> query.getOperationGroup().equals(o.getOperationGroup())).collect(Collectors.toList());
            if (ChargeTypeGroupEnum.WITHDRAW.equals(query.getOperationGroup())) {
                query.setType(ChargeType.withdraw);
            }
        }

        if (Objects.nonNull(query.getOperationType())) {
            orderChargeTypes = orderChargeTypes.stream()
                    .filter(o -> query.getOperationType().equals(o.getOperationType())).collect(Collectors.toList());
            if (OperationTypeEnum.WITHDRAW.equals(query.getOperationType())) {
                query.setType(ChargeType.withdraw);
            }
        }

        List<ChargeType> chargeTypes =
                orderChargeTypes.stream().map(OrderChargeType::getType).collect(Collectors.toList());
        chargeTypes.add(ChargeType.withdraw);
        if (Objects.nonNull(query.getType())) {
            chargeTypes = List.of(query.getType());
        }

        query.setTypes(chargeTypes);
        IPage<WalletChargeFlowVo> list = accountBalanceOperationLogMapper.list(pageQuery.page(), query);

        return list.convert(walletChargeFlowVo -> {
            ChargeType chargeType = walletChargeFlowVo.getChargeType();
            chargeType = chargeType.accountWrapper(walletChargeFlowVo.getLogType());

            OrderChargeType orderChargeType = orderChargeTypeMap.get(chargeType);
            walletChargeFlowVo.setChargeTypeName(walletChargeFlowVo.getChargeType().getNameZn());
            walletChargeFlowVo.setOperationGroup(orderChargeType.getOperationGroup());
            walletChargeFlowVo.setOperationType(orderChargeType.getOperationType());
            return walletChargeFlowVo;
        });
    }
}
