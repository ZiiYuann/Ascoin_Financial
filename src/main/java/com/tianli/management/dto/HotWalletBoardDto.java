package com.tianli.management.dto;

import com.tianli.account.vo.AccountBalanceSimpleVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-31
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotWalletBoardDto {

    private List<AccountBalanceSimpleVO> accountBalances;

    private List<AmountDto> rechargeAmounts;

    private List<AmountDto> withdrawAmounts;

    private List<AmountDto> financialAmounts;

    private List<AmountDto> fundAmounts;
}
