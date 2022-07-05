package com.tianli.exchange.service;

import com.tianli.exchange.entity.ExchangeMsgFail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 交易消息处理失败表 服务类
 * </p>
 *
 * @author lzy
 * @since 2022-07-01
 */
public interface IExchangeMsgFailService extends IService<ExchangeMsgFail> {

    ExchangeMsgFail getByMsgId(Long id);
}
