package com.tianli.borrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.bo.BorrowPledgeCoinConfigBO;
import com.tianli.borrow.contant.BorrowOrderPledgeStatus;
import com.tianli.borrow.convert.BorrowCoinConfigConverter;
import com.tianli.borrow.dao.BorrowCoinOrderMapper;
import com.tianli.borrow.entity.BorrowPledgeCoinConfig;
import com.tianli.borrow.dao.BorrowPledgeCoinConfigMapper;
import com.tianli.borrow.service.IBorrowPledgeCoinConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.vo.BorrowPledgeCoinConfigVO;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

/**
 * <p>
 * 质押币种配置 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-07-28
 */
@Service
public class BorrowPledgeCoinConfigServiceImpl extends ServiceImpl<BorrowPledgeCoinConfigMapper, BorrowPledgeCoinConfig> implements IBorrowPledgeCoinConfigService {

    @Autowired
    private BorrowCoinConfigConverter borrowCoinConfigConverter;
    @Autowired
    private BorrowPledgeCoinConfigMapper borrowPledgeCoinConfigMapper;
    @Autowired
    private BorrowCoinOrderMapper borrowCoinOrderMapper;
    @Override
    public void saveConfig(BorrowPledgeCoinConfigBO bo) {
        verifyPledgeRate(bo);
        bo.convertToRate();
        BorrowPledgeCoinConfig borrowPledgeCoinConfig = borrowCoinConfigConverter.toPledgeDO(bo);
        if(Objects.nonNull(getByCoin(bo.getCoin()))) ErrorCodeEnum.BORROW_CONFIG_EXIST.throwException();
        borrowPledgeCoinConfig.setCreateTime(LocalDateTime.now());
        borrowPledgeCoinConfigMapper.insert(borrowPledgeCoinConfig);
    }

    @Override
    public void updateConfig(BorrowPledgeCoinConfigBO bo) {
        verifyPledgeRate(bo);
        bo.convertToRate();
        BorrowPledgeCoinConfig borrowPledgeCoinConfig = borrowCoinConfigConverter.toPledgeDO(bo);
        BorrowPledgeCoinConfig configByCoin = borrowCoinConfigConverter.toPledgeDO(bo);
        BorrowPledgeCoinConfig configById = borrowPledgeCoinConfigMapper.selectById(bo.getId());
        //校验配置
        if(Objects.isNull(configById)) ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();
        if(Objects.nonNull(configByCoin) && !configByCoin.getCoin().equals(configById.getCoin()))ErrorCodeEnum.BORROW_CONFIG_EXIST.throwException();
        borrowPledgeCoinConfigMapper.updateById(borrowPledgeCoinConfig);
        //修改订单质押状态
        BigDecimal warnPledgeRate = borrowPledgeCoinConfig.getWarnPledgeRate();
        BigDecimal liquidationPledgeRate = borrowPledgeCoinConfig.getLiquidationPledgeRate();
        //预警线上移
        if(bo.getWarnPledgeRate().compareTo(configById.getWarnPledgeRate()) > 0) {
            borrowCoinOrderMapper.updatePledgeStatusByPledgeRate(null, warnPledgeRate, BorrowOrderPledgeStatus.SAFE_PLEDGE);
        }

        //预警线下移 平仓线上移
        if(bo.getWarnPledgeRate().compareTo(configById.getWarnPledgeRate()) < 0
        || bo.getLiquidationPledgeRate().compareTo(configById.getLiquidationPledgeRate()) > 0) {
            borrowCoinOrderMapper.updatePledgeStatusByPledgeRate(warnPledgeRate, liquidationPledgeRate, BorrowOrderPledgeStatus.WARN_PLEDGE);
        }

        //平仓线上移
        if(bo.getLiquidationPledgeRate().compareTo(configById.getLiquidationPledgeRate()) < 0){
            borrowCoinOrderMapper.updatePledgeStatusByPledgeRate(liquidationPledgeRate, null, BorrowOrderPledgeStatus.LIQUIDATION_PLEDGE);
        }

    }

    @Override
    public void delConfig(Long[] ids) {
        Arrays.asList(ids).forEach(id ->{
            BorrowPledgeCoinConfig borrowPledgeCoinConfig = borrowPledgeCoinConfigMapper.selectById(id);
            if(Objects.isNull(borrowPledgeCoinConfig)) ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();
            Integer count = borrowCoinOrderMapper.selectCountByPledgeCoin(borrowPledgeCoinConfig.getCoin());
            if(count > 0) ErrorCodeEnum.BORROW_CONFIG_USED.throwException();
            borrowPledgeCoinConfigMapper.deleteById(id);
        });
    }

    @Override
    public IPage<BorrowPledgeCoinConfigVO> pageList(PageQuery<BorrowPledgeCoinConfig> pageQuery, CurrencyCoin coin) {
        LambdaQueryWrapper<BorrowPledgeCoinConfig> queryWrapper = new QueryWrapper<BorrowPledgeCoinConfig>().lambda();
        if(Objects.nonNull(coin)){
            queryWrapper.eq(BorrowPledgeCoinConfig::getCoin,coin.getName());
        }
        return borrowPledgeCoinConfigMapper.selectPage(pageQuery.page(),queryWrapper).convert(borrowCoinConfigConverter::toPledgeVO);
    }

    @Override
    public BorrowPledgeCoinConfig getByCoin(CurrencyCoin coin) {
        return borrowPledgeCoinConfigMapper.selectOne(new QueryWrapper<BorrowPledgeCoinConfig>().lambda()
                .eq(BorrowPledgeCoinConfig::getCoin,coin.getName()));
    }

    void verifyPledgeRate(BorrowPledgeCoinConfigBO bo){
        if(bo.getInitialPledgeRate().compareTo(bo.getWarnPledgeRate()) >= 0
        || bo.getWarnPledgeRate().compareTo(bo.getLiquidationPledgeRate()) >= 0)
            ErrorCodeEnum. BORROW_CONFIG_RATE_ERROR.throwException();
    }
}
