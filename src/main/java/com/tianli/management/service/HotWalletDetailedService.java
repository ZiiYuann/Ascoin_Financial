package com.tianli.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.service.contract.ContractOperation;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.management.enums.HotWalletOperationType;
import com.tianli.management.mapper.HotWalletDetailedMapper;
import com.tianli.management.query.HotWalletDetailedIoUQuery;
import com.tianli.management.query.HotWalletDetailedPQuery;
import com.tianli.management.vo.HotWalletBalanceVO;
import com.tianli.management.vo.HotWalletDetailedSummaryDataVO;
import com.tianli.management.vo.HotWalletDetailedVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-17
 **/
@Service
public class HotWalletDetailedService extends ServiceImpl<HotWalletDetailedMapper, HotWalletDetailed> {

    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private OrderService orderService;
    @Resource
    private AddressService addressService;
    @Resource
    private ContractAdapter contractAdapter;

    /**
     * 【热钱包管理】添加明细 或 修改明细
     */
    @Transactional
    public void insertOrUpdate(HotWalletDetailedIoUQuery ioUQuery) {
        if (!HotWalletOperationType.recharge.equals(ioUQuery.getType()) && !HotWalletOperationType.withdraw.equals(ioUQuery.getType())) {
            ErrorCodeEnum.throwException("类型传值错误");
        }

        HotWalletDetailed hotWalletDetailed = managementConverter.toDO(ioUQuery);
        if (Objects.isNull(hotWalletDetailed.getId())) {
            hotWalletDetailed.setId(CommonFunction.generalId());
            hotWalletDetailed.setCreateTime(LocalDateTime.now());
            baseMapper.insert(hotWalletDetailed);
        }

        if (Objects.nonNull(hotWalletDetailed.getId())) {
            baseMapper.updateById(hotWalletDetailed);
        }
    }

    /**
     * 插入数据
     */
    @Transactional
    public void insert(HotWalletDetailed hotWalletDetailed) {
        baseMapper.insert(hotWalletDetailed);
    }


    @Transactional
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }

    public IPage<HotWalletDetailedVO> pageByQuery(Page<HotWalletDetailed> page, HotWalletDetailedPQuery query) {


        return baseMapper.pageByQuery(page, query).convert(managementConverter::toHotWalletDetailedVO);

    }

    public HotWalletDetailedSummaryDataVO SummaryData(HotWalletDetailedPQuery query) {

        query.setType(HotWalletOperationType.recharge);
        BigDecimal rechargeAmountDollar = orderService.calDollarAmount(baseMapper.summaryDataByQuery(query));

        query.setType(HotWalletOperationType.withdraw);
        BigDecimal withdrawAmountDollar = orderService.calDollarAmount(baseMapper.summaryDataByQuery(query));

        query.setType(HotWalletOperationType.user_withdraw);
        BigDecimal userWithdrawAmountDollar = orderService.calDollarAmount(baseMapper.summaryDataByQuery(query));

        query.setType(HotWalletOperationType.imputation);
        BigDecimal imputationAmountDollar = orderService.calDollarAmount(baseMapper.summaryDataByQuery(query));

        return HotWalletDetailedSummaryDataVO.builder()
                .rechargeAmountDollar(rechargeAmountDollar)
                .withdrawAmountDollar(withdrawAmountDollar)
                .userWithdrawAmountDollar(userWithdrawAmountDollar)
                .imputationAmountDollar(imputationAmountDollar).build();
    }


    public HotWalletBalanceVO balance() {
        Address configAddress = addressService.getConfigAddress();
        HotWalletBalanceVO vo = new HotWalletBalanceVO();

        String bsc = configAddress.getBsc();
        String eth = configAddress.getEth();
        String tron = configAddress.getTron();

        ContractOperation ethContract = contractAdapter.getOne(NetworkType.erc20);
        ContractOperation bscContract = contractAdapter.getOne(NetworkType.bep20);
        ContractOperation tronContract = contractAdapter.getOne(NetworkType.trc20);

        vo.setBnb(TokenAdapter.bnb.alignment(bscContract.mainBalance(bsc)));
        vo.setUsdcBep20(TokenAdapter.usdc_bep20.alignment(bscContract.tokenBalance(bsc, TokenAdapter.usdc_bep20)));
        vo.setUsdtBep20(TokenAdapter.usdt_bep20.alignment(bscContract.tokenBalance(bsc, TokenAdapter.usdt_bep20)));

        vo.setEth(TokenAdapter.eth.alignment(ethContract.mainBalance(eth)));
        vo.setUsdcERC20(TokenAdapter.usdc_erc20.alignment(ethContract.tokenBalance(eth, TokenAdapter.usdc_erc20)));
        vo.setUsdtERC20(TokenAdapter.usdt_erc20.alignment(ethContract.tokenBalance(eth, TokenAdapter.usdt_erc20)));

        vo.setTrx(TokenAdapter.trx.alignment(tronContract.mainBalance(tron)));
        vo.setUsdcTRC20(TokenAdapter.usdc_trc20.alignment(tronContract.tokenBalance(tron, TokenAdapter.usdc_trc20)));
        vo.setUsdtTRC20(TokenAdapter.usdt_trc20.alignment(tronContract.tokenBalance(tron, TokenAdapter.usdt_trc20)));

        return vo;
    }
}
