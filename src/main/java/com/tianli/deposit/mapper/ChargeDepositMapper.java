package com.tianli.deposit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.currency.enums.TokenAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.jdbc.SQL;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 充值提现表  保证金 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-19
 */
@Mapper
public interface ChargeDepositMapper extends BaseMapper<ChargeDeposit> {

    @SelectProvider(type = GenerateSQL.class, method = "selectSumAmount")
    Map<String, BigDecimal> selectSumAmount(@Param("nike") String nike,
                                            @Param("phone") String phone,
                                            @Param("txid") String txid,
                                            @Param("status") ChargeDepositStatus status,
                                            @Param("type") String type,
                                            @Param("startTime") String startTime,
                                            @Param("endTime") String endTime,
                                            @Param("chargeDepositType") ChargeDepositType chargeDepositType,
                                            @Param("tokenCurrencyType") TokenAdapter tokenAdapter);

    @Update("UPDATE `charge_deposit` SET `complete_time`=#{complete_time},`status`=#{success},`txid`=#{txid} WHERE `id`=#{id} AND `status`=#{transacting}")
    int updateSuccess(@Param("complete_time") LocalDateTime complete_time, @Param("success") ChargeDepositStatus success,
                      @Param("txid") String txid, @Param("id") Long id, @Param("transacting") ChargeDepositStatus transacting);

    @Update("UPDATE `charge_deposit` SET `complete_time`=#{complete_time},`status`=#{fail} WHERE `id`=#{id} AND `status`=#{transacting}")
    int updateFail(@Param("complete_time") LocalDateTime complete_time, @Param("fail") ChargeDepositStatus fail, @Param("id") long id, @Param("transacting") ChargeDepositStatus transacting);

    class GenerateSQL {
        public String selectSumAmount(String nike,
                                      String phone,
                                      String txid,
                                      ChargeDepositStatus status,
                                      String type,
                                      String startTime,
                                      String endTime,
                                      ChargeDepositType chargeDepositType,
                                      TokenAdapter tokenAdapter) {
            SQL sql = new SQL()
                    .SELECT(" ifnull(SUM(`amount`), 0) as allAmount, " +
                            "ifnull(SUM(CASE WHEN `status` = 'transacting' OR `status` = 'created' THEN `amount` ELSE 0 END), 0) as executingAmount, " +
                            "ifnull(SUM(CASE WHEN `status` = 'transaction_success' THEN `amount` ELSE 0 END), 0) as sucAmount ")
                    .FROM(" `charge_deposit` ");
            if (StringUtils.isNotBlank(nike)) {
                sql.WHERE(" `uid_nick` like CONCAT('%',#{nike},'%') ");
            }
            if (StringUtils.isNotBlank(phone)) {
                sql.WHERE(" `uid_username` like CONCAT('%',#{phone},'%') ");
            }
            if (StringUtils.isNotBlank(txid)) {
                sql.WHERE(" `txid` like CONCAT('%',#{txid},'%') ");
            }
            if (Objects.nonNull(status)) {
                sql.WHERE(" `status` = #{status}");
            }
            if (Objects.nonNull(type)) {
                sql.WHERE(" `settlement_type` = #{type}");
            }
            if (StringUtils.isNotBlank(startTime)) {
                sql.WHERE(" `create_time` >= #{startTime}");
            }
            if (StringUtils.isNotBlank(endTime)) {
                sql.WHERE(" `create_time` <= #{endTime}");
            }
            if (Objects.nonNull(chargeDepositType)) {
                sql.WHERE(" `charge_type` = #{chargeDepositType}");
            }
            if (Objects.nonNull(tokenAdapter)) {
                sql.WHERE(" `currency_type` = #{tokenCurrencyType}");
            }
            return sql.toString();
        }
    }
}
