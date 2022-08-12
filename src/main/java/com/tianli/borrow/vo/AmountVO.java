package com.tianli.borrow.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmountVO implements Serializable {

    private static final long serialVersionUID=1L;

    private BigDecimal amount;
}
