package com.tianli.borrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.bo.BorrowOrderConfigBO;
import com.tianli.borrow.contant.BorrowPledgeRate;
import com.tianli.borrow.convert.BorrowCoinConfigConverter;
import com.tianli.borrow.entity.BorrowCoinConfig;
import com.tianli.borrow.dao.BorrowCoinConfigMapper;
import com.tianli.borrow.service.IBorrowCoinConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.vo.BorrowCoinConfigVO;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * <p>
 * 借币数据配置 服务实现类
 * </p>
 *
 * @author xn
 * @since 2022-07-21
 */
@Service
public class BorrowCoinConfigServiceImpl extends ServiceImpl<BorrowCoinConfigMapper, BorrowCoinConfig> implements IBorrowCoinConfigService {

    @Autowired
    private BorrowCoinConfigConverter borrowCoinConfigConverter;
    @Autowired
    private BorrowCoinConfigMapper borrowCoinConfigMapper;

    @Override
    public void saveConfig(BorrowOrderConfigBO bo) {
        BorrowCoinConfig coinConfig = this.getByCoin(bo.getCoin());
        if(Objects.isNull(coinConfig)){
            BorrowCoinConfig borrowCoinConfig = borrowCoinConfigConverter.toDO(bo);
            borrowCoinConfig.setCreateTime(new Date());
            borrowCoinConfig.setInitialPledgeRate(BorrowPledgeRate.INITIAL_PLEDGE_RATE);
            borrowCoinConfig.setWarnPledgeRate(BorrowPledgeRate.WARN_PLEDGE_RATE);
            borrowCoinConfig.setLiquidationPledgeRate(BorrowPledgeRate.LIQUIDATION_PLEDGE_RATE);
            borrowCoinConfigMapper.insert(borrowCoinConfig);
        }else {
            coinConfig.setAnnualInterestRate(bo.getAnnualInterestRate());
            coinConfig.setMaximumBorrow(bo.getMaximumBorrow());
            coinConfig.setMinimumBorrow(bo.getMinimumBorrow());
            borrowCoinConfigMapper.updateById(coinConfig);
        }
    }

    @Override
    public void updateConfig(BorrowOrderConfigBO bo) {
        BorrowCoinConfig borrowCoinConfig = borrowCoinConfigMapper.selectById(bo.getId());
        if(Objects.isNull(borrowCoinConfig)) ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();
        BorrowCoinConfig updateBorrowCoinConfig = borrowCoinConfigConverter.toDO(bo);
        borrowCoinConfigMapper.updateById(updateBorrowCoinConfig);
    }

    @Override
    public void delConfig(Long[] id) {
        BorrowCoinConfig borrowCoinConfig = borrowCoinConfigMapper.selectById(id);
        if(Objects.isNull(borrowCoinConfig)) ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();
        Arrays.asList(id).forEach(borrowCoinConfigMapper::loginDel);
    }

    @Override
    public IPage<BorrowCoinConfigVO> pageList(PageQuery<BorrowCoinConfig> pageQuery, CurrencyCoin coin) {

        LambdaQueryWrapper<BorrowCoinConfig> queryWrapper = new QueryWrapper<BorrowCoinConfig>().lambda();

        if(!Objects.isNull(coin)){
            queryWrapper.like(BorrowCoinConfig::getCoin,coin.getName());
        }

        return borrowCoinConfigMapper.selectPage(pageQuery.page(),queryWrapper).convert(borrowCoinConfigConverter::toVO);
    }

    @Override
    public BorrowCoinConfig getByCoin(CurrencyCoin coin) {
        return borrowCoinConfigMapper.selectOne(new QueryWrapper<BorrowCoinConfig>().lambda()
                .eq(BorrowCoinConfig::getCoin,coin)
                .eq(BorrowCoinConfig::getIsDel,0)
        );
    }
}