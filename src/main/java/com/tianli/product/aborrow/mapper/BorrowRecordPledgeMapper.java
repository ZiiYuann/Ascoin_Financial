package com.tianli.product.aborrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.aborrow.entity.BorrowRecordPledge;
import com.tianli.product.aborrow.enums.PledgeType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
@Mapper
public interface BorrowRecordPledgeMapper extends BaseMapper<BorrowRecordPledge> {


    @Update(" UPDATE  borrow_record_pledge SET amount = amount + #{increaseAmount}  WHERE" +
            " uid =#{uid} AND coin =#{coin} and amount =#{originalAmount} and pledge_type=#{pledgeType}")
    int casIncrease(@Param("uid") Long uid,
                    @Param("coin") String coin,
                    @Param("increaseAmount") BigDecimal increaseAmount,
                    @Param("originalAmount") BigDecimal originalAmount,
                    @Param("pledgeType") PledgeType pledgeType);


    @Update(" UPDATE  borrow_record_pledge SET amount = amount - #{decreaseAmount}  WHERE" +
            " uid =#{uid} AND coin =#{coin} and amount =#{originalAmount} AND amount >= #{decreaseAmount}" +
            "and pledge_type=#{pledgeType} ")
    int casDecrease(@Param("uid") Long uid,
                    @Param("coin") String coin,
                    @Param("decreaseAmount") BigDecimal decreaseAmount,
                    @Param("originalAmount") BigDecimal originalAmount,
                    @Param("pledgeType") PledgeType pledgeType);
}
