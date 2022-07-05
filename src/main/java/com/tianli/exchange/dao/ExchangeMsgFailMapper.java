package com.tianli.exchange.dao;

import com.tianli.exchange.entity.ExchangeMsgFail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 交易消息处理失败表 Mapper 接口
 * </p>
 *
 * @author lzy
 * @since 2022-07-01
 */
@Mapper
public interface ExchangeMsgFailMapper extends BaseMapper<ExchangeMsgFail> {

}
