package com.tianli.chain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author cs
 * @Date 2023-01-04 21:01
 */
@Data
@AllArgsConstructor
public class BtcBalance {
    private String balance;
    private Integer countUnspent;
}
