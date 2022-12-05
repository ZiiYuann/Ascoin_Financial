package com.tianli.chain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-05
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoinMapVO {

    private String name;

    private int withdrawDecimals;

    private List<CoinVO> coins;

}
