package com.tianli.management.mapper;

import com.tianli.management.dto.FinancialUserListDto;
import com.tianli.management.dto.FinancialUserRecordListDto;
import com.tianli.management.dto.FinancialUserTotalDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigInteger;
import java.util.List;

/**
 * @author lzy
 * @date 2022/4/1 6:31 下午
 */
@Mapper
public interface FinancialUserMapper {

    Long total(@Param("financialUserListDto") FinancialUserListDto financialUserListDto);

    List<FinancialUserRecordListDto> page(@Param("page") Integer page, @Param("size") Integer size, @Param("financialUserListDto") FinancialUserListDto financialUserListDto);

    FinancialUserTotalDto selectTotalAmount(@Param("financialUserListDto") FinancialUserListDto financialUserListDto);

    BigInteger selectTotalCurrentAmount(@Param("financialUserListDto") FinancialUserListDto financialUserListDto);
}
