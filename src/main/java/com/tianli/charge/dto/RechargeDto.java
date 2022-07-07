package com.tianli.charge.dto;

import com.tianli.address.mapper.Address;
import com.tianli.currency.CurrencyTokenEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigInteger;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-07
 **/
@Data
@AllArgsConstructor
public class RechargeDto {

    Address address;

    BigInteger finalAmount;

    CurrencyTokenEnum token;

}
