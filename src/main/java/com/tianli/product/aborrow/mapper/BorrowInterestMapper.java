package com.tianli.product.aborrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.aborrow.entity.BorrowInterest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-14
 **/
@Mapper
public interface BorrowInterestMapper extends BaseMapper<BorrowInterest> {

    @Update(" UPDATE  borrow_interest SET amount = amount + #{increaseAmount}  WHERE" +
            " uid =#{uid} AND coin =#{coin} and amount =#{originalAmount}")
    int casIncrease(@Param("uid") Long uid,
                    @Param("coin") String coin,
                    @Param("increaseAmount") BigDecimal increaseAmount,
                    @Param("originalAmount") BigDecimal originalAmount);


    @Update(" UPDATE  borrow_interest SET amount = amount - #{decreaseAmount}  WHERE" +
            " uid =#{uid} AND coin =#{coin} and amount =#{originalAmount} AND amount >= #{decreaseAmount} ")
    int casDecrease(@Param("uid") Long uid,
                    @Param("coin") String coin,
                    @Param("decreaseAmount") BigDecimal decreaseAmount,
                    @Param("originalAmount") BigDecimal originalAmount);
}
