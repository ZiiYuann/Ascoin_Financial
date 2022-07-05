package com.tianli.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.chain.service.StatCollectAmount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wangqiyun
 * @since 2020/11/16 21:01
 */

@Mapper
public interface ChainTxMapper extends BaseMapper<ChainTx> {
    @Select("SELECT `currency_type`, SUM(`amount`) as total_amount FROM `chain_tx` WHERE `status` = 'chain_success' GROUP BY `currency_type`")
    List<StatCollectAmount> totalAmount();
}
