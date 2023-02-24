package com.tianli.charge.service;

import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
public interface OrderAdvanceProcessor {

    AdvanceType getType();

    void verifier(GenerateOrderAdvanceQuery query);

    void preInsertProcess(GenerateOrderAdvanceQuery query, OrderAdvance orderAdvance);
}
