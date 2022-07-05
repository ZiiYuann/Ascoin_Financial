package com.tianli.management.salesman.vo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.management.salesman.entity.Salesman;
import com.tianli.management.salesman.entity.SalesmanUser;
import com.tianli.management.spot.entity.SGCharge;
import com.tianli.user.mapper.User;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author lzy
 * @date 2022/4/7 3:21 下午
 */
@Builder
@Data
public class SalesmanInfoListVo {

    private Long id;

    private String username;
    /**
     * 充值金额
     */
    @Builder.Default
    private BigDecimal recharge_amount = BigDecimal.ZERO;
    /**
     * 提现金额
     */
    @Builder.Default
    private BigDecimal withdrawal_amount = BigDecimal.ZERO;
    /**
     * 利润
     */
    @Builder.Default
    private BigDecimal profit = BigDecimal.ZERO;

    @Builder.Default
    private Integer user_count = 0;

    private String creator;

    private LocalDateTime create_time;

    private String remark;

    private String kf_url;

    public static SalesmanInfoListVo getSalesmanInfoListVo(Salesman salesman, List<SalesmanUser> salesmanUsers, Map<Long, List<Charge>> chargeMap, Map<Long, List<SGCharge>> sgChargeMap, Map<Long, User> userMap) {
        SalesmanInfoListVoBuilder infoListVoBuilder = SalesmanInfoListVo.builder()
                .id(salesman.getId())
                .username(salesman.getAdmin_username())
                .creator(salesman.getCreator())
                .create_time(salesman.getCreate_time())
                .remark(salesman.getRemark())
                .kf_url(salesman.getKf_url());
        if (CollUtil.isEmpty(salesmanUsers)) {
            return infoListVoBuilder.build();
        }
        infoListVoBuilder.user_count(salesmanUsers.size());
        BigDecimal recharge_amount = BigDecimal.ZERO;
        BigDecimal withdrawal_amount = BigDecimal.ZERO;
        for (SalesmanUser salesmanUser : salesmanUsers) {
            //如果是员工直接跳过
            if (CollUtil.isNotEmpty(userMap) && ObjectUtil.isNotNull(userMap.get(salesmanUser.getUser_id())) && ObjectUtil.equal(userMap.get(salesmanUser.getUser_id()).getUser_type(), 1)) {
                continue;
            }
            List<Charge> chargeList = chargeMap.get(salesmanUser.getUser_id());
            if (CollUtil.isNotEmpty(chargeList)) {
                for (Charge charge : chargeList) {
                    TokenCurrencyType tokenCurrencyType = TokenCurrencyType.getTokenCurrencyType(charge.getToken().name());
                    if (ObjectUtil.isNotNull(tokenCurrencyType)) {
                        if (charge.getCharge_type().equals(ChargeType.recharge)) {
                            recharge_amount = recharge_amount.add(Convert.toBigDecimal(tokenCurrencyType.money(charge.getAmount())));
                        } else {
                            withdrawal_amount = withdrawal_amount.add(Convert.toBigDecimal(tokenCurrencyType.money(charge.getAmount())));
                        }
                    }
                }
            }
            List<SGCharge> sgChargeList = sgChargeMap.get(salesmanUser.getUser_id());
            if (CollUtil.isNotEmpty(sgChargeList)) {
                for (SGCharge sgCharge : sgChargeList) {
                    if (ObjectUtil.equal(ChargeType.recharge, sgCharge.getCharge_type())) {
                        recharge_amount = recharge_amount.add(sgCharge.getAmount());
                    } else {
                        withdrawal_amount = withdrawal_amount.add(sgCharge.getAmount());
                    }
                }
            }
        }
        infoListVoBuilder.recharge_amount(recharge_amount);
        infoListVoBuilder.withdrawal_amount(withdrawal_amount);
        infoListVoBuilder.profit(recharge_amount.subtract(withdrawal_amount));
        return infoListVoBuilder.build();
    }
}
