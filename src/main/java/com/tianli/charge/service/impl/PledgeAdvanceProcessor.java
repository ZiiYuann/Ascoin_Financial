package com.tianli.charge.service.impl;

import cn.hutool.json.JSONUtil;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.service.OrderAdvanceProcessor;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.aborrow.enums.ModifyPledgeContextType;
import com.tianli.product.aborrow.query.ModifyPledgeContextQuery;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Service
public class PledgeAdvanceProcessor implements OrderAdvanceProcessor {
    @Override
    public AdvanceType getType() {
        return AdvanceType.PLEDGE;
    }

    @Override
    public void verifier(GenerateOrderAdvanceQuery query) {
        ModifyPledgeContextQuery modifyPledgeContextQuery =
                Optional.ofNullable(query.getModifyPledgeContextQuery())
                        .orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);

        if (!ModifyPledgeContextType.ADD.equals(modifyPledgeContextQuery.getType())) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }

    }

    @Override
    public void preInsertProcess(GenerateOrderAdvanceQuery query, OrderAdvance orderAdvance) {
        orderAdvance.setQuery(JSONUtil.toJsonStr(query.getModifyPledgeContextQuery()));
    }
}
