package com.tianli.account.query;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.tianli.charge.enums.ChargeGroup;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.annotation.QueryWrapperGenerator;
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

    @QueryWrapperGenerator
    private Long uid;

    @QueryWrapperGenerator
    private String coin;

    @QueryWrapperGenerator(op = SqlKeyword.NE)
    private ChargeStatus status = ChargeStatus.chain_fail;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @QueryWrapperGenerator(op = SqlKeyword.GE, field = "create_time")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @QueryWrapperGenerator(op = SqlKeyword.LE, field = "create_time")
    private LocalDateTime endTime;

    @QueryWrapperGenerator(op = SqlKeyword.IN, field = "type")
    private List<ChargeType> chargeTypes;

    @QueryWrapperGenerator(op = SqlKeyword.DESC, field = "create_time")
    private Boolean orderByCreateTimeDesc = Boolean.TRUE;

    @QueryWrapperGenerator(op = SqlKeyword.DESC, field = "id")
    private Boolean orderByIdDesc = Boolean.TRUE;

    private ChargeType chargeType;

    private ChargeGroup chargeGroup;

    private List<ChargeGroup> chargeGroups;

    public Set<ChargeType> chargeTypes() {
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
