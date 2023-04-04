package com.tianli.account.query;

import com.tianli.charge.enums.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author:yangkang
 * @create: 2023-03-13 18:41
 * @Description: 云钱包流水记录新查询参数
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDetailsNewQuery implements Serializable {

    private String coin;

    private List<ChargeType> chargeType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private List<BalanceOperationChargeTypeQuery> chargeTypeQueries;

    public void setChargeType(List<ChargeType> chargeType) {
        List<ChargeType> needRemoveChargeType = new ArrayList<>();

        List<BalanceOperationChargeTypeQuery> queries = new ArrayList<>();
        chargeType = Optional.ofNullable(chargeType).orElse(Collections.emptyList());
        chargeType.forEach(type -> {
            BalanceOperationChargeTypeQuery query = ChargeType.balanceOperationChargeTypeQuery(type);
            if (Objects.nonNull(query)) {
                queries.add(query);
                needRemoveChargeType.add(type);
            }
        });
        chargeType.removeAll(needRemoveChargeType);
        this.chargeType = chargeType;
        this.chargeTypeQueries = queries;
    }
}
