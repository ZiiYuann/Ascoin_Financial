package com.tianli.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.chain.entity.WalletImputation;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.query.WalletImputationQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Mapper
public interface WalletImputationMapper extends BaseMapper<WalletImputation> {


    @Update("UPDATE wallet_imputation SET `amount` = `amount` + #{increaseAmount},`update_time` = now() WHERE `id` =#{id}")
    int increase(Long id, BigDecimal increaseAmount);


    List<AmountDto> imputationAmount(WalletImputationQuery query);

}
