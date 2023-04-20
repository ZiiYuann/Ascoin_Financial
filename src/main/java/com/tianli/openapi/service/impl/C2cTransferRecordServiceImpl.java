package com.tianli.openapi.service.impl;

import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.openapi.dto.IdDto;
import com.tianli.openapi.entity.C2cTransferRecord;
import com.tianli.openapi.entity.C2cTransferRecordMapper;
import com.tianli.openapi.query.OpenapiC2CQuery;
import com.tianli.openapi.service.IC2cTransferRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2023-04-12
 */
@Service
public class C2cTransferRecordServiceImpl extends ServiceImpl<C2cTransferRecordMapper, C2cTransferRecord> implements IC2cTransferRecordService {
    @Resource
    OrderService orderService;

    @Resource
    AccountBalanceService accountBalanceService;

    @Override
    public IdDto c2cTransfer(OpenapiC2CQuery query) {
        ChargeType chargeType = query.getChargeType();
        if (!chargeType.equals(ChargeType.c2c_transfer_in) && !chargeType.equals(ChargeType.c2c_transfer_out)) {
            ErrorCodeEnum.throwException("当前接口交易类型不匹配");
        }
        long c2cTransferRecordId = CommonFunction.generalId();
        LocalDateTime now = LocalDateTime.now();
        long tId = CommonFunction.generalId();
        String s = CommonFunction.generalSn(tId);
        Order c2ctransferOrder = Order.builder()
                .id(tId)
                .uid(query.getUid())
                .orderNo(chargeType.getAccountChangeType().getPrefix() + s)
                .type(chargeType)
                .status(ChargeStatus.chain_success)
                .coin(query.getCoin())
                .amount(query.getAmount())
                .createTime(now)
                .completeTime(LocalDateTime.now())
                .relatedId(c2cTransferRecordId)
                .build();
        orderService.save(c2ctransferOrder);
        C2cTransferRecord c2cTransferRecord = C2cTransferRecord.builder()
                .id(c2cTransferRecordId)
                .uid(query.getUid())
                .amount(query.getAmount())
                .coin(query.getCoin())
                .c2cOrderNo(c2ctransferOrder.getOrderNo())
                .chargeType(chargeType)
                .externalPk(query.getRelatedId())
                .createTime(LocalDateTime.now())
                .build();
        this.save(c2cTransferRecord);
        if (query.getChargeType().equals(ChargeType.c2c_transfer_in)) {
            accountBalanceService.c2cTransferIn(query.getUid(), chargeType, query.getCoin(),
                    query.getAmount(), c2cTransferRecord.getC2cOrderNo(), null);

        } else {
            accountBalanceService.c2cTransferOut(query.getUid(), chargeType, query.getCoin(),
                    query.getAmount(), c2cTransferRecord.getC2cOrderNo(), null);
        }
        return new IdDto(c2cTransferRecordId);
    }
}
