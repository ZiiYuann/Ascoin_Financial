package com.tianli.management.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.PageQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.fund.contant.FundIncomeStatus;
import com.tianli.fund.contant.FundTransactionStatus;
import com.tianli.fund.enums.FundTransactionType;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundTransactionRecordService;
import com.tianli.management.bo.WalletAgentBO;
import com.tianli.management.converter.WalletAgentConverter;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.entity.WalletAgent;
import com.tianli.management.dao.WalletAgentMapper;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.query.WalletAgentQuery;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.management.service.IWalletAgentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.management.vo.WalletAgentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 云钱包代理人 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Service
@Transactional
public class WalletAgentServiceImpl extends ServiceImpl<WalletAgentMapper, WalletAgent> implements IWalletAgentService {

    @Autowired
    private WalletAgentConverter walletAgentConverter;

    @Autowired
    private WalletAgentMapper walletAgentMapper;

    @Autowired
    private IWalletAgentProductService walletAgentProductService;

    @Autowired
    private FinancialProductService financialProductService;

    @Autowired
    private AccountBalanceService accountBalanceService;

    @Resource
    private OrderService orderService;

    @Autowired
    private IFundIncomeRecordService fundIncomeRecordService;

    @Autowired
    private IFundTransactionRecordService fundTransactionRecordService;

    @Override
    public void saveAgent(WalletAgentBO bo) {
        Integer count = walletAgentMapper.selectCountByUid(bo.getUid());
        if(count > 0) ErrorCodeEnum.AGENT_ALREADY_BIND.throwException();
        List<WalletAgentBO.Product> products = bo.getProducts();
        WalletAgent walletAgent = walletAgentConverter.toDO(bo);
        walletAgent.setCreateTime(LocalDateTime.now());
        walletAgent.setLoginPassword(SecureUtil.md5(walletAgent.getAgentName()));
        walletAgentMapper.insert(walletAgent);
        products.forEach(product -> {
            FinancialProduct financialProduct = financialProductService.getById(product.getProductId());
            if(Objects.isNull(financialProduct) || !financialProduct.getType().equals(ProductType.fund)) ErrorCodeEnum.AGENT_PRODUCT_NOT_EXIST.throwException();
            if(walletAgentProductService.getCount(product.getProductId()) > 0) ErrorCodeEnum.AGENT_PRODUCT_ALREADY_BIND.throwException();
            WalletAgentProduct agentProduct = WalletAgentProduct.builder()
                    .productId(product.getProductId())
                    .productName(financialProduct.getName())
                    .agentId(walletAgent.getId())
                    .referralCode(product.getReferralCode())
                    .uid(walletAgent.getUid())
                    .build();
            walletAgentProductService.save(agentProduct);
        });
    }

    @Override
    public void updateAgent(WalletAgentBO bo) {
        WalletAgent walletAgent = walletAgentMapper.selectById(bo.getId());
        WalletAgent saveAgent = walletAgentConverter.toDO(bo);
        if(Objects.isNull(walletAgent))ErrorCodeEnum.AGENT_NOT_EXIST.throwException();
        if(!walletAgent.getUid().equals(saveAgent.getUid())){
            Integer count = walletAgentMapper.selectCountByUid(bo.getUid());
            if(count > 0) ErrorCodeEnum.AGENT_ALREADY_BIND.throwException();
        }
        walletAgentMapper.updateById(saveAgent);
        walletAgentProductService.deleteByAgentId(saveAgent.getId());
        List<WalletAgentProduct> products = walletAgentProductService.getByAgentId(walletAgent.getId());
        //删除多余的
        List<Long> productIds = products.stream().map(WalletAgentProduct::getProductId).collect(Collectors.toList());
        List<WalletAgentBO.Product> saveProducts = bo.getProducts();
        List<Long> saveProductIds = saveProducts.stream().map(WalletAgentBO.Product::getProductId).collect(Collectors.toList());
        productIds.removeAll(saveProductIds);
        productIds.forEach(id -> walletAgentProductService.deleteByProductId(id));
        //增加新的



    }

    @Override
    public void delAgent(Long id) {
        WalletAgent walletAgent = walletAgentMapper.selectById(id);
        if(Objects.isNull(walletAgent))ErrorCodeEnum.AGENT_NOT_EXIST.throwException();
        //其对应基金产品需手动操作下线
        List<WalletAgentProduct> walletAgentProducts = walletAgentProductService.getByAgentId(walletAgent.getId());
        walletAgentProducts.forEach(walletAgentProduct -> {
            FinancialProduct financialProduct = financialProductService.getById(walletAgentProduct.getProductId());
            if(financialProduct.getStatus() == ProductStatus.open){
                ErrorCodeEnum.PRODUCT_NOT_CLOSE.throwException();
            }
        });
        //代理人存在待赎回金额、待发放利息时不可删除
        boolean waitRedemption = fundTransactionRecordService.existWaitRedemption(walletAgent.getUid());
        if(waitRedemption) ErrorCodeEnum.EXIST_WAIT_REDEMPTION.throwException();
        boolean waitInterest = fundIncomeRecordService.existWaitInterest(walletAgent.getUid());
        if(waitInterest) ErrorCodeEnum.EXIST_WAIT_INTEREST.throwException();
        walletAgentMapper.logicDelById(id);
        walletAgentProductService.deleteByAgentId(id);
    }

    @Override
    public WalletAgentVO getById(Long id) {
        WalletAgent walletAgent = walletAgentMapper.selectById(id);
        if(Objects.isNull(walletAgent))ErrorCodeEnum.AGENT_NOT_EXIST.throwException();
        WalletAgentVO walletAgentVO = walletAgentConverter.toVO(walletAgent);
        List<WalletAgentProduct> walletAgentProducts = walletAgentProductService.list(new QueryWrapper<WalletAgentProduct>().lambda()
                .eq(WalletAgentProduct::getAgentId, id));
        List<WalletAgentVO.Product> products = walletAgentProducts.stream().map(walletAgentProduct -> WalletAgentVO.Product.builder()
                .productId(walletAgentProduct.getProductId())
                .productName(walletAgentProduct.getProductName())
                .referralCode(walletAgentProduct.getReferralCode())
                .build()).collect(Collectors.toList());
        walletAgentVO.setProducts(products);
        return walletAgentVO;
    }

    @Override
    public IPage<WalletAgentVO> getPage(PageQuery<WalletAgent> pageQuery, WalletAgentQuery query) {
        IPage<WalletAgentVO> page = new Page<>(pageQuery.getPage(),pageQuery.getPageSize());
        walletAgentMapper.selectPageByQuery(page, query);
        page.convert(walletAgentVO -> {
            Long uid = walletAgentVO.getUid();
            Long id = walletAgentVO.getId();
            List<WalletAgentProduct> walletAgentProductList = walletAgentProductService.getByAgentId(id);
            List<WalletAgentVO.Product> productList = walletAgentProductList.stream().map(walletAgentProduct -> WalletAgentVO.Product.builder()
                    .productId(walletAgentProduct.getProductId())
                    .productName(walletAgentProduct.getProductName())
                    .build()).collect(Collectors.toList());
            walletAgentVO.setWalletAmount(getWalletAmount(uid));
            walletAgentVO.setRechargeAmount(getRechargeAmount(uid));
            walletAgentVO.setWithdrawAmount(getWithdrawAmount(uid));
            walletAgentVO.setProducts(productList);
            walletAgentVO.setHoldAmount(getHoldAmount(uid));
            walletAgentVO.setRedemptionAmount(getRedemptionAmount(uid));
            walletAgentVO.setInterestAmount(getInterestAmount(uid, FundIncomeStatus.audit_success));
            walletAgentVO.setWaitInterestAmount(getInterestAmount(uid, FundIncomeStatus.wait_audit));
            return walletAgentVO;
        });
        return page;
    }

    @Override
    public WalletAgent getByAgentName(String agentName) {
        return walletAgentMapper.selectOne(new QueryWrapper<WalletAgent>().lambda()
                .eq(WalletAgent::getAgentName,agentName)
                .eq(WalletAgent::getDeleted,0)
        );
    }

    private void saveAgentProduct(WalletAgent walletAgent, List<WalletAgentBO.Product> products) {
        products.forEach(product -> {
            FinancialProduct financialProduct = financialProductService.getById(product.getProductId());
            if(Objects.isNull(financialProduct) || !financialProduct.getType().equals(ProductType.fund)) ErrorCodeEnum.AGENT_PRODUCT_NOT_EXIST.throwException();
            if(walletAgentProductService.getCount(product.getProductId()) > 0) ErrorCodeEnum.AGENT_PRODUCT_ALREADY_BIND.throwException();
            WalletAgentProduct agentProduct = WalletAgentProduct.builder()
                    .productId(product.getProductId())
                    .productName(financialProduct.getName())
                    .agentId(walletAgent.getId())
                    .referralCode(product.getReferralCode())
                    .uid(walletAgent.getUid())
                    .build();
            walletAgentProductService.save(agentProduct);
        });
    }

    private BigDecimal getWalletAmount(Long uid){
        List<AccountBalanceVO> accountBalanceList = accountBalanceService.getAccountBalanceList(uid);
        List<AmountDto> amountDtoList = accountBalanceList.stream().map(accountBalanceVO ->
                new AmountDto(accountBalanceVO.getRemain(), accountBalanceVO.getCoin())).collect(Collectors.toList());
        return orderService.calDollarAmount(amountDtoList);
    }

    private BigDecimal getRechargeAmount(Long uid){
        return orderService.amountDollarSumByChargeType(uid,ChargeType.recharge);
    }

    private BigDecimal getWithdrawAmount(Long uid){
        return orderService.amountDollarSumByChargeType(uid,ChargeType.withdraw);
    }

    private BigDecimal getHoldAmount(Long agentUId){
        List<AmountDto> amountDtos = walletAgentMapper.holdAmountSum(agentUId);
        return orderService.calDollarAmount(amountDtos);
    }

    private BigDecimal getRedemptionAmount(Long uid){
        List<AmountDto> amountDtos = walletAgentMapper.redemptionAmountSum(uid, FundTransactionType.redemption, FundTransactionStatus.wait_audit);
        return orderService.calDollarAmount(amountDtos);
    }

    private BigDecimal getInterestAmount(Long uid, Integer status){
        List<AmountDto> amountDtos = walletAgentMapper.interestAmountSum(uid, status);
        return orderService.calDollarAmount(amountDtos);
    }

}
