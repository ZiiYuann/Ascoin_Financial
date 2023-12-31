package com.tianli.management.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundProductBindDropdownVO {

    private Long productId;

    private String productName;

    private String productNameEn;
}
