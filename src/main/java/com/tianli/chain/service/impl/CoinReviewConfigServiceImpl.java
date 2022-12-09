package com.tianli.chain.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.entity.CoinReviewConfig;
import com.tianli.chain.mapper.CoinReviewConfigMapper;
import com.tianli.chain.query.CoinReviewConfigIoUQuery;
import com.tianli.chain.service.CoinReviewConfigService;
import com.tianli.common.CommonFunction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-09
 **/
@Service
public class CoinReviewConfigServiceImpl extends ServiceImpl<CoinReviewConfigMapper, CoinReviewConfig> implements CoinReviewConfigService {

    @Resource
    private CoinReviewConfigMapper coinReviewConfigMapper;
    @Resource
    private ChainConverter chainConverter;

    @Override
    @Transactional
    public void reviewConfig(String nickname, CoinReviewConfigIoUQuery query) {

        CoinReviewConfig coinReviewConfig = chainConverter.toDO(query);

        coinReviewConfigMapper.softDelete();
        coinReviewConfig.setCreateBy(nickname);
        coinReviewConfig.setId(CommonFunction.generalId());
        coinReviewConfigMapper.insert(coinReviewConfig);

    }

    @Override
    public CoinReviewConfig reviewConfig() {
        return coinReviewConfigMapper.selectUq();
    }

}
