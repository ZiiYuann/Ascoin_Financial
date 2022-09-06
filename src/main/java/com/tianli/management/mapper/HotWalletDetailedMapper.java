package com.tianli.management.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.query.HotWalletDetailedPQuery;
import com.tianli.management.entity.HotWalletDetailed;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * @author chenb
 * @apiNote
 * @since 2022-08-17
 **/
@Mapper
public interface HotWalletDetailedMapper extends BaseMapper<HotWalletDetailed> {

    IPage<HotWalletDetailed> pageByQuery(@Param("page") Page<HotWalletDetailed> page,
                                         @Param("query") HotWalletDetailedPQuery query);

    List<AmountDto> summaryDataByQuery(@Param("query") HotWalletDetailedPQuery query);

}
