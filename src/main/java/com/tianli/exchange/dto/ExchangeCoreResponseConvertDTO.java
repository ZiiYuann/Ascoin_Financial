package com.tianli.exchange.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author lzy
 * @date 2022/6/24 11:31
 */
@Data
public class ExchangeCoreResponseConvertDTO {

    private Long id;

    private Long time;

    private String symbol;

    private Integer operator;

    private Long cancel_id;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 手数
     */
    private BigDecimal quantity;

    private Long buy_id;

    private Long sell_id;

    public static ExchangeCoreResponseConvertDTO get(ExchangeCoreResponseDTO coreResponseDTO) {
        ExchangeCoreResponseConvertDTO exchangeCoreResponseConvertDTO = BeanUtil.copyProperties(coreResponseDTO, ExchangeCoreResponseConvertDTO.class, "price", "quantity");
        exchangeCoreResponseConvertDTO.setPrice(Convert.toBigDecimal(coreResponseDTO.getPrice()).divide(new BigDecimal("1000")));
        exchangeCoreResponseConvertDTO.setQuantity(Convert.toBigDecimal(coreResponseDTO.getQuantity()).divide(new BigDecimal("1000")));
        return exchangeCoreResponseConvertDTO;
    }
}
