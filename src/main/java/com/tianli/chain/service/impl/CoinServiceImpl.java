package com.tianli.chain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.mapper.CoinMapper;
import com.tianli.chain.service.CoinService;
import com.tianli.currency.service.CurrencyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-21
 **/
@Service
public class CoinServiceImpl extends ServiceImpl<CoinMapper, Coin> implements CoinService {

    @Resource
    private CoinMapper coinMapper;
    @Resource
    private CurrencyService currencyService;


}
