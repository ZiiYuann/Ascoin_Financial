package com.tianli.account.vo;

import com.tianli.charge.enums.ChargeGroup;
import lombok.Data;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-03
 **/
@Data
public class TransactionGroupTypeVO {

    private ChargeGroup group;

    private List<TransactionTypeVO> types;


}
