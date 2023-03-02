package com.tianli.charge.service.impl;

import cn.hutool.json.JSONUtil;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.entity.BorrowConfigPledge;
import com.tianli.product.aborrow.enums.ModifyPledgeContextType;
import com.tianli.product.aborrow.enums.PledgeType;
import com.tianli.product.aborrow.query.ModifyPledgeContextQuery;
import com.tianli.product.aborrow.query.PledgeContextQuery;
import com.tianli.product.aborrow.service.BorrowConfigPledgeService;
import com.tianli.product.aborrow.service.BorrowService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Service
public class PledgeAdvanceProcessor extends AbstractAdvanceProcessor<ModifyPledgeContextQuery> {

    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private BorrowConfigPledgeService borrowConfigPledgeService;
    @Resource
    private BorrowService borrowService;

    @Override
    protected ChargeType chargeType() {
        return ChargeType.pledge;
    }

    @Override
    public AdvanceType getType() {
        return AdvanceType.PLEDGE;
    }

    @Override
    public void verifier(GenerateOrderAdvanceQuery query) {
        ModifyPledgeContextQuery modifyPledgeContextQuery =
                Optional.ofNullable(query.getModifyPledgeContextQuery())
                        .orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);

        List<PledgeContextQuery> pledgeContext = modifyPledgeContextQuery.getPledgeContext();
        if (!ModifyPledgeContextType.ADD.equals(modifyPledgeContextQuery.getType()) ||
                CollectionUtils.isEmpty(pledgeContext)) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        pledgeContext = pledgeContext.stream().filter(index -> PledgeType.WALLET.equals(index.getPledgeType()))
                .collect(Collectors.toList());
        if (pledgeContext.size() != 1) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        PledgeContextQuery pledgeContextQuery = pledgeContext.get(0);
        if (pledgeContextQuery.getPledgeAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        Set<String> coinNames = coinBaseService.pushCoinNames();
        if (!coinNames.contains(pledgeContextQuery.getCoin())) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        BorrowConfigPledge borrowConfigPledge = borrowConfigPledgeService.getById(pledgeContextQuery.getCoin());
        if (Objects.isNull(borrowConfigPledge) || borrowConfigPledge.getStatus() != 1) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
    }

    @Override
    public void preInsertProcess(GenerateOrderAdvanceQuery query, OrderAdvance orderAdvance) {
        orderAdvance.setQuery(JSONUtil.toJsonStr(query.getModifyPledgeContextQuery()));
    }

    @Override
    protected void handlerRechargerOperation(OrderAdvance orderAdvance, TRONTokenReq tronTokenReq, BigDecimal finalAmount, Coin coin) {
        ModifyPledgeContextQuery query = this.getQuery(orderAdvance);
        borrowService.modifyPledgeContext(orderAdvance.getUid(), query);
    }

    @Override
    protected Order generateOrder(OrderAdvance orderAdvance) {
        ModifyPledgeContextQuery query = this.getQuery(orderAdvance);
        Optional<PledgeContextQuery> any = query.getPledgeContext().stream().filter(index -> PledgeType.WALLET.equals(index.getPledgeType()))
                .findAny();

        if (any.isEmpty()) {
            throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
        }

        PledgeContextQuery pledgeContextQuery = any.get();
        return Order.generate(orderAdvance.getId(),
                this.chargeType(), pledgeContextQuery.getCoin(), pledgeContextQuery.getPledgeAmount(), orderAdvance.getId());
    }

    @Override
    public ModifyPledgeContextQuery getQuery(OrderAdvance orderAdvance) {
        return JSONUtil.toBean(orderAdvance.getQuery(), ModifyPledgeContextQuery.class);
    }

}
