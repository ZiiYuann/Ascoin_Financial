package com.tianli.product.aborrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.aborrow.entity.BorrowRecordCoin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface BorrowRecordCoinMapper extends BaseMapper<BorrowRecordCoin> {

    @Update(" UPDATE  borrow_record_coin SET amount = amount + #{increaseAmount}  WHERE" +
            " uid =#{uid} AND coin =#{coin} and amount =#{originalAmount}")
    int casIncrease(@Param("uid") Long uid,
                    @Param("coin") String coin,
                    @Param("increaseAmount") BigDecimal increaseAmount,
                    @Param("originalAmount") BigDecimal originalAmount);


    @Update(" UPDATE  borrow_record_coin SET amount = amount - #{decreaseAmount}  WHERE" +
            " uid =#{uid} AND coin =#{coin} and amount =#{originalAmount} AND amount >= #{decreaseAmount} ")
    int casDecrease(@Param("uid") Long uid,
                    @Param("coin") String coin,
                    @Param("decreaseAmount") BigDecimal decreaseAmount,
                    @Param("originalAmount") BigDecimal originalAmount);
}
