package com.tianli.account.query;

import com.tianli.charge.enums.ChargeGroup;
import com.tianli.charge.enums.ChargeType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-04
 **/
@Data
@NoArgsConstructor
public class AccountDetailsQuery {

    private ChargeGroup chargeGroup;

    private String coin;

    private ChargeType chargeType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private List<ChargeGroup> chargeGroups;

    private List<ChargeType> chargeTypes;

    public Set<ChargeType> chargeTypeSet() {
        Set<ChargeType> types = new HashSet<>();

        Optional.ofNullable(this.getChargeGroup()).ifPresent(group -> types.addAll(group.getChargeTypes()));

        if (CollectionUtils.isNotEmpty(this.getChargeGroups())) {
            this.getChargeGroups().forEach(group -> types.addAll(group.getChargeTypes()));
        }
        if (CollectionUtils.isNotEmpty(this.getChargeTypes())) {
            types.addAll(this.getChargeTypes());
        }

        if (Objects.nonNull(chargeType)) {
            types.addAll(chargeTypes);
        }
        return types;
    }

}
