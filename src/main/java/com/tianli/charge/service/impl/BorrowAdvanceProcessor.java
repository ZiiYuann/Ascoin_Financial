package com.tianli.charge.service.impl;

import cn.hutool.json.JSONUtil;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.service.OrderAdvanceProcessor;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Service
public class BorrowAdvanceProcessor implements OrderAdvanceProcessor {
    @Override
    public AdvanceType getType() {
        return AdvanceType.BORROW;
    }

    @Override
    public void verifier(GenerateOrderAdvanceQuery query) {
        Optional.ofNullable(query.getBorrowCoinQuery())
                .orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
    }

    @Override
    public void preInsertProcess(GenerateOrderAdvanceQuery query, OrderAdvance orderAdvance) {
        orderAdvance.setQuery(JSONUtil.toJsonStr(query.getBorrowCoinQuery()));
    }
}
