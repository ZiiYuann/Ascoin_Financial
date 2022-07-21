package com.tianli.borrow.dao;

import com.tianli.borrow.entity.BorrowCoinOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * <p>
 * 借币订单 Mapper 接口
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Mapper
public interface BorrowCoinOrderMapper extends BaseMapper<BorrowCoinOrder> {

    @Select("SELECT SUM(borrow_capital) FROM borrow_coin_order")
    BigDecimal selectTotalBorrowAmount();

    @Select("SELECT SUM(borrow_capital) FROM borrow_coin_order where uid = #{uid}")
    BigDecimal selectBorrowAmountByUid(Long uid);

    @Select("SELECT SUM(pledge_amount) FROM borrow_coin_order where uid = #{uid}")
    BigDecimal selectPledgeAmountByUid(Long uid);
}
