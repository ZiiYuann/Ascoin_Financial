package com.tianli.exchange.push;

import cn.hutool.core.convert.Convert;
import com.tianli.exchange.dto.ExchangeDepthDTO;
import com.tianli.exchange.dto.ExchangeDepthGearDTO;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lzy
 * @date 2022/6/27 11:08
 */
@Data
@Builder
public class DepthStream implements Serializable {
    private List<List<String>> b;

    private List<List<String>> a;


    public static DepthStream getDepthStream(ExchangeDepthDTO exchangeDepthDTO) {
        return DepthStream.builder()
                .b(getDepth(exchangeDepthDTO.getBuyRank()))
                .a(getDepth(exchangeDepthDTO.getSellRank()))
                .build();
    }

    private static List<List<String>> getDepth(List<ExchangeDepthGearDTO> rank) {
        List<List<String>> ranks = new ArrayList<>();
        for (ExchangeDepthGearDTO exchangeDepthGearDTO : rank) {
            BigDecimal price = Convert.toBigDecimal(exchangeDepthGearDTO.getPrice()).divide(new BigDecimal("1000"));
            BigDecimal number = Convert.toBigDecimal(exchangeDepthGearDTO.getNumber()).divide(new BigDecimal("1000"));
            List<String> list = new ArrayList<>();
            list.add(price.toString());
            list.add(number.toString());
            ranks.add(list);
        }
        return ranks;
    }
}
