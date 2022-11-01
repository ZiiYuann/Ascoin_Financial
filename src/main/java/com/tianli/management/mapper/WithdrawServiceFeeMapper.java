package com.tianli.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.entity.WithdrawServiceFee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-27
 **/
@Mapper
public interface WithdrawServiceFeeMapper extends BaseMapper<WithdrawServiceFee> {


    @Select("SELECT sum(eth) as eth, sum(bnb) as bnb,sum(trx) as trx  FROM withdraw_service_fee ")
    WithdrawServiceFee getTotalAmount();
}
