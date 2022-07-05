package com.tianli.dividends.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;

/**
 * <p>
 * 分红表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@Mapper
public interface DividendsMapper extends BaseMapper<Dividends> {

    @Select("SELECT IFNULL(SUM(`my_profit`), 0) FROM `dividends` WHERE dividends_uid = #{dividendsUid}")
    BigInteger sumAmount(@Param("dividendsUid") Long uid);
}
