package com.tianli.currency_token.transfer.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency_token.transfer.mapper.TxLog;
import com.tianli.currency_token.transfer.mapper.TxLogMapper;
import org.springframework.stereotype.Service;

@Service
public class TxLogService extends ServiceImpl<TxLogMapper, TxLog> {
}
