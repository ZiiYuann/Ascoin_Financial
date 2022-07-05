package com.tianli.loan.dao;

import com.tianli.loan.entity.Loan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.loan.vo.LoanListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
@Mapper
public interface LoanMapper extends BaseMapper<Loan> {

    Long count(@Param("username") String username,
               @Param("status") String status,
               @Param("reviewer") String reviewer,
               @Param("startTime") String startTime,
               @Param("endTime") String endTime);

    List<LoanListVo> queryList(@Param("username") String username,
                               @Param("status") String status,
                               @Param("reviewer") String reviewer,
                               @Param("startTime") String startTime,
                               @Param("endTime") String endTime,
                               @Param("page") Integer page,
                               @Param("size") Integer size);

    BigDecimal total(@Param("username") String username,
                     @Param("status") String status,
                     @Param("reviewer") String reviewer,
                     @Param("startTime") String startTime,
                     @Param("endTime") String endTime);
}
