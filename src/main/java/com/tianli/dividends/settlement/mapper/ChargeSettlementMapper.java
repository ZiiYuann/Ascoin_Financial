package com.tianli.dividends.settlement.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.currency.TokenCurrencyType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 充值提现表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-11
 */
@Mapper
public interface ChargeSettlementMapper extends BaseMapper<ChargeSettlement> {

    @Update("UPDATE `charge_settlement` SET `complete_time`=#{complete_time},`status`=#{success} ,`miner_fee`=#{miner_fee},`miner_fee_type` = #{miner_fee_type} WHERE `id`=#{id} AND `status`=#{transacting}")
    int updateSuccess(@Param("complete_time") LocalDateTime complete_time, @Param("success") ChargeSettlementStatus success,
                      @Param("id") Long id, @Param("transacting") ChargeSettlementStatus transacting,
                      @Param("miner_fee") BigInteger miner_fee, @Param("miner_fee_type") TokenCurrencyType miner_fee_type);

    @Update("UPDATE `charge_settlement` SET `complete_time`=#{complete_time},`status`=#{fail}, `miner_fee`=#{miner_fee},`miner_fee_type` = #{miner_fee_type} WHERE `id`=#{id} AND `status`=#{transacting}")
    int updateFail(@Param("complete_time") LocalDateTime complete_time, @Param("fail") ChargeSettlementStatus fail,
                   @Param("id") long id, @Param("transacting") ChargeSettlementStatus transacting,
                   @Param("miner_fee") BigInteger miner_fee, @Param("miner_fee_type") TokenCurrencyType miner_fee_type);
}
