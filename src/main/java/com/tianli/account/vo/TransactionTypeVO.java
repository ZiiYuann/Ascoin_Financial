package com.tianli.account.vo;

import com.tianli.charge.enums.ChargeType;
import lombok.Data;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-03
 **/
@Data
public class TransactionTypeVO {

    private ChargeType type;

    private String name;

    private String nameEn;


}
