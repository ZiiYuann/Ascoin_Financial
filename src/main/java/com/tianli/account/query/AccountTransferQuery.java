package com.tianli.account.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountTransferQuery {

    @NotBlank
    private Long toChatId;

    private Long fromChatId;

    @NotBlank
    private String coin;

    @DecimalMin("0.00")
    private BigDecimal amount;

    private boolean addressBook;

    private String addressBookRemarks;

    private boolean repeatCheck;
}
