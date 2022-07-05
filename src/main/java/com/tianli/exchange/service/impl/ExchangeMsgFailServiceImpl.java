package com.tianli.exchange.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.exchange.entity.ExchangeMsgFail;
import com.tianli.exchange.dao.ExchangeMsgFailMapper;
import com.tianli.exchange.service.IExchangeMsgFailService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 交易消息处理失败表 服务实现类
 * </p>
 *
 * @author lzy
 * @since 2022-07-01
 */
@Service
public class ExchangeMsgFailServiceImpl extends ServiceImpl<ExchangeMsgFailMapper, ExchangeMsgFail> implements IExchangeMsgFailService {

    @Override
    public ExchangeMsgFail getByMsgId(Long id) {
        ExchangeMsgFail exchangeMsgFail = null;
        List<ExchangeMsgFail> exchangeMsgFailList = this.list(Wrappers.lambdaQuery(ExchangeMsgFail.class).eq(ExchangeMsgFail::getMsg_id, id));
        if (CollUtil.isNotEmpty(exchangeMsgFailList)) {
            exchangeMsgFail = exchangeMsgFailList.get(0);
        }
        return exchangeMsgFail;
    }
}
