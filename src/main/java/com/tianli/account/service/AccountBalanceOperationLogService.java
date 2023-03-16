package com.tianli.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.entity.AccountBalanceOperationLog;
import com.tianli.charge.enums.ChargeTypeGroupEnum;
import com.tianli.charge.enums.NewChargeType;
import com.tianli.charge.enums.WithdrawChargeTypeEnum;
import com.tianli.account.mapper.AccountBalanceOperationLogMapper;
import com.tianli.account.vo.WalletChargeFlowVo;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.management.query.WalletChargeFlowQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    /**
     * 添加余额操作日志
     */
    public void save(AccountBalance accountBalance, ChargeType type, String coin, NetworkType networkType
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
    }


    public IPage<WalletChargeFlowVo> capitalFlowList(PageQuery<AccountBalanceOperationLog> pageQuery, WalletChargeFlowQuery walletChargeFlowQuery) {
        if (StringUtils.isNotEmpty(walletChargeFlowQuery.getType()) && walletChargeFlowQuery.getType().contains(WithdrawChargeTypeEnum.withdraw.name())) {
            String withdrawType = WithdrawChargeTypeEnum.getTypeByDesc(walletChargeFlowQuery.getType());
            walletChargeFlowQuery.setWithdrawType(withdrawType);
        }
        //去掉基金利息类型
        IPage<WalletChargeFlowVo> list = accountBalanceOperationLogMapper.list(pageQuery.page(),
                walletChargeFlowQuery,NewChargeType.fund_interest.name(),WithdrawChargeTypeEnum.withdraw.getType());
        return list.convert(walletChargeFlowVo -> {
            {
                if (walletChargeFlowVo.getChargeType().equals(WithdrawChargeTypeEnum.withdraw.name())) {
                    walletChargeFlowVo.setOperationType(WithdrawChargeTypeEnum.withdraw.getDescription());
                    walletChargeFlowVo.setOperationGroupName(ChargeTypeGroupEnum.withdraw.getTypeGroup());
                    String description = WithdrawChargeTypeEnum.getDescriptionByType(walletChargeFlowVo.getLogType());
                    walletChargeFlowVo.setChargeTypeName(description);
                } else {
                    walletChargeFlowVo.setOperationGroupName(ChargeTypeGroupEnum.getTypeGroup(walletChargeFlowVo.getOperationGroup()));
                }
                return walletChargeFlowVo;
            }
        });
    }
}
