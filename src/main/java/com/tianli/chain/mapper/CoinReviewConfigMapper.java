package com.tianli.chain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.chain.entity.CoinReviewConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-09
 **/
@Mapper
public interface CoinReviewConfigMapper extends BaseMapper<CoinReviewConfig> {

    /**
     * 获取唯一配置
     */
    @Select("SELECT  * FROM  `coin_review_config` WHERE  `deleted` = 0")
    CoinReviewConfig selectUq();

    /**
     * 获取唯一配置
     */
    @Update("UPDATE `coin_review_config` SET `deleted` = 1 WHERE  `deleted` = 0")
    int softDelete();

}
