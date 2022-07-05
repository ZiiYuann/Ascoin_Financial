package com.tianli.kline.task;


import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

@Data
public class HuoBiTradeRes {
    private String ch;
    private String status;
    private Long ts;
    private Tick tick;
    @Data
    class Tick{
        private Long id;
        private Long ts;
        private List<Trade> data;
        @Data
        class Trade{
            private BigInteger id;
            private Long ts;
//            private Long trade-id;
            private BigDecimal amount;
            private Double price;
            private String direction;
        }
    }

    public Tick.Trade getFirstTrade(){
        Tick tick = getTick();
        if(Objects.isNull(tick)){
            return null;
        }
        List<Tick.Trade> data = tick.getData();
        if(CollectionUtils.isEmpty(data)){
            return null;
        }
        return data.get(0);
    }
}
