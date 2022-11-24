package com.tianli.borrow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.bo.BorrowOrderConfigBO;
import com.tianli.borrow.convert.BorrowCoinConfigConverter;
import com.tianli.borrow.dao.BorrowCoinConfigMapper;
import com.tianli.borrow.entity.BorrowCoinConfig;
import com.tianli.borrow.service.IBorrowCoinConfigService;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.tianli.borrow.vo.BorrowCoinConfigVO;
import com.tianli.common.PageQuery;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
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
@Transactional
public class BorrowCoinConfigServiceImpl extends ServiceImpl<BorrowCoinConfigMapper, BorrowCoinConfig> implements IBorrowCoinConfigService {

    @Resource
    private BorrowCoinConfigConverter borrowCoinConfigConverter;
    @Resource
    private BorrowCoinConfigMapper borrowCoinConfigMapper;
    @Resource
    private IBorrowCoinOrderService borrowCoinOrderService;

    @Override
    public void saveConfig(BorrowOrderConfigBO bo) {
        bo.convertToRate();
        BorrowCoinConfig coinConfig = this.getByCoin(bo.getCoin());
        BorrowCoinConfig borrowCoinConfig = borrowCoinConfigConverter.toDO(bo);
        if(Objects.nonNull(coinConfig))ErrorCodeEnum.BORROW_CONFIG_EXIST.throwException();
        borrowCoinConfig.setCreateTime(LocalDateTime.now());
        borrowCoinConfigMapper.insert(borrowCoinConfig);
    }

    @Override
    public void updateConfig(BorrowOrderConfigBO bo) {
        bo.convertToRate();
        BorrowCoinConfig borrowCoinConfig = borrowCoinConfigMapper.selectById(bo.getId());
        BorrowCoinConfig coinConfig = this.getByCoin(bo.getCoin());
        if(Objects.isNull(borrowCoinConfig)) ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();
        if(Objects.nonNull(coinConfig) && !coinConfig.getCoin().equals(borrowCoinConfig.getCoin()))ErrorCodeEnum.BORROW_CONFIG_EXIST.throwException();
        BorrowCoinConfig updateBorrowCoinConfig = borrowCoinConfigConverter.toDO(bo);
        borrowCoinConfigMapper.updateById(updateBorrowCoinConfig);
    }

    @Override
    public void delConfig(Long[] ids) {
        Arrays.asList(ids).forEach(id ->{
            BorrowCoinConfig borrowCoinConfig = borrowCoinConfigMapper.selectById(id);
            if(Objects.isNull(borrowCoinConfig)) ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();
            Integer count = borrowCoinOrderService.selectCountByBorrowCoin(borrowCoinConfig.getCoin());
            if(count > 0) ErrorCodeEnum.BORROW_CONFIG_USED.throwException();
            borrowCoinConfigMapper.deleteById(id);
        });
    }

    @Override
    public IPage<BorrowCoinConfigVO> pageList(PageQuery<BorrowCoinConfig> pageQuery, String coin) {

        LambdaQueryWrapper<BorrowCoinConfig> queryWrapper = new QueryWrapper<BorrowCoinConfig>().lambda();

        if(!Objects.isNull(coin)){
            queryWrapper.like(BorrowCoinConfig::getCoin,coin);
        }
        return borrowCoinConfigMapper.selectPage(pageQuery.page(),queryWrapper).convert(borrowCoinConfigConverter::toVO);
    }

    @Override
    public BorrowCoinConfig getByCoin(String coin) {
        return borrowCoinConfigMapper.selectOne(new QueryWrapper<BorrowCoinConfig>().lambda()
                .eq(BorrowCoinConfig::getCoin,coin)
        );
    }
}
