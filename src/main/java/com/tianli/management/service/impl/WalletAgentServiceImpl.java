package com.tianli.management.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.tianli.management.bo.WalletAgentBO;
import com.tianli.management.converter.WalletAgentConverter;
import com.tianli.management.dao.WalletAgentMapper;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.entity.WalletAgent;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.query.WalletAgentQuery;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.management.service.IWalletAgentService;
import com.tianli.management.vo.WalletAgentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
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

    @Override
    public void saveAgent(WalletAgentBO bo) {
        Integer count = walletAgentMapper.selectCountByUid(bo.getUid());
        if (count > 0) ErrorCodeEnum.AGENT_ALREADY_BIND.throwException();
        List<WalletAgentBO.Product> products = bo.getProducts();
        WalletAgent walletAgent = walletAgentConverter.toDO(bo);
        walletAgent.setCreateTime(LocalDateTime.now());
        walletAgent.setLoginPassword(SecureUtil.md5(walletAgent.getAgentName()));
        walletAgentMapper.insert(walletAgent);
        products.forEach(product -> saveAgentProduct(walletAgent, product));
    }

    @Override
    public void updateAgent(WalletAgentBO bo) {
        WalletAgent walletAgent = walletAgentMapper.selectById(bo.getId());
        if (Objects.isNull(walletAgent)) ErrorCodeEnum.AGENT_NOT_EXIST.throwException();
        walletAgent.setRemark(bo.getRemark());
        walletAgentMapper.updateById(walletAgent);

        // 校验是否绑定了其他已经绑定的产品
        List<Long> otherBindProductIds = walletAgentProductService.listProductIdExcludeAgentId(bo.getId());
        bo.getProducts().forEach(operationProduct -> {
            if (otherBindProductIds.contains(operationProduct.getProductId())) {
                ErrorCodeEnum.FUND_PRODUCT_REPEAT_BIND.throwException();
            }
        });

        List<WalletAgentProduct> products = walletAgentProductService.getByAgentId(walletAgent.getId());
        //删除多余的
        List<Long> productIds = products.stream().map(WalletAgentProduct::getProductId).collect(Collectors.toList());
        List<WalletAgentBO.Product> saveProducts = bo.getProducts();
        List<Long> saveProductIds = saveProducts.stream().map(WalletAgentBO.Product::getProductId).collect(Collectors.toList());
        Collection<Long> deletedIds = CollUtil.subtract(productIds, saveProductIds);
        deletedIds.forEach(id -> walletAgentProductService.deleteByProductId(id));
        //增加或更新
        saveProducts.forEach(saveProduct -> {
            List<WalletAgentProduct> updateProduct = products.stream().filter(product ->
                    product.getProductId().equals(saveProduct.getProductId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(updateProduct)) {
                saveAgentProduct(walletAgent, saveProduct);
            } else {
                updateProduct.forEach(product -> {
                    product.setReferralCode(saveProduct.getReferralCode());
                    walletAgentProductService.updateById(product);
                });
            }
        });
    }

    @Override
    public void delAgent(Long id) {
        WalletAgent walletAgent = walletAgentMapper.selectById(id);
        if (Objects.isNull(walletAgent)) ErrorCodeEnum.AGENT_NOT_EXIST.throwException();
        //其对应基金产品需手动操作下线 代理人存在待赎回金额、待发放利息时不可删除
        List<WalletAgentProduct> walletAgentProducts = walletAgentProductService.getByAgentId(walletAgent.getId());
        walletAgentProducts.forEach(walletAgentProduct -> {
            FinancialProduct financialProduct = financialProductService.getById(walletAgentProduct.getProductId());
            if (financialProduct.getStatus() == ProductStatus.open) {
                ErrorCodeEnum.PRODUCT_NOT_CLOSE.throwException();
            }
            walletAgentProductService.deleteByProductId(walletAgentProduct.getId());
        });
        walletAgentMapper.logicDelById(id);
    }

    @Override
    public WalletAgentVO getById(Long id) {
        WalletAgent walletAgent = walletAgentMapper.selectById(id);
        if (Objects.isNull(walletAgent)) ErrorCodeEnum.AGENT_NOT_EXIST.throwException();
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
        IPage<WalletAgentVO> page = new Page<>(pageQuery.getPage(), pageQuery.getPageSize());
        walletAgentMapper.selectPageByQuery(page, query);
        page.convert(walletAgentVO -> {
            Long uid = walletAgentVO.getUid();
            Long agentId = walletAgentVO.getId();
            List<WalletAgentProduct> walletAgentProductList = walletAgentProductService.getByAgentId(agentId);
            List<WalletAgentVO.Product> productList = walletAgentProductList.stream().map(walletAgentProduct -> WalletAgentVO.Product.builder()
                    .productId(walletAgentProduct.getProductId())
                    .productName(walletAgentProduct.getProductName())
                    .build()).collect(Collectors.toList());
            walletAgentVO.setWalletAmount(getWalletAmount(uid));
            walletAgentVO.setRechargeAmount(getRechargeAmount(uid));
            walletAgentVO.setWithdrawAmount(getWithdrawAmount(uid));
            walletAgentVO.setProducts(productList);
            walletAgentVO.setHoldAmount(getHoldAmount(agentId));
            walletAgentVO.setRedemptionAmount(getRedemptionAmount(agentId));
            walletAgentVO.setInterestAmount(getInterestAmount(agentId, FundIncomeStatus.audit_success));
            walletAgentVO.setWaitInterestAmount(getInterestAmount(agentId, List.of(FundIncomeStatus.wait_audit, FundIncomeStatus.calculated)));
            return walletAgentVO;
        });
        return page;
    }

    @Override
    public WalletAgent getByAgentName(String agentName) {
        return walletAgentMapper.selectOne(new QueryWrapper<WalletAgent>().lambda()
                .eq(WalletAgent::getAgentName, agentName)
                .eq(WalletAgent::getDeleted, 0)
        );
    }

    private void saveAgentProduct(WalletAgent walletAgent, WalletAgentBO.Product product) {
        FinancialProduct financialProduct = financialProductService.getById(product.getProductId());
        if (Objects.isNull(financialProduct) || !financialProduct.getType().equals(ProductType.fund))
            ErrorCodeEnum.AGENT_PRODUCT_NOT_EXIST.throwException();
        if (walletAgentProductService.getCount(product.getProductId()) > 0)
            ErrorCodeEnum.AGENT_PRODUCT_ALREADY_BIND.throwException();
        WalletAgentProduct agentProduct = WalletAgentProduct.builder()
                .productId(product.getProductId())
                .productName(financialProduct.getName())
                .agentId(walletAgent.getId())
                .referralCode(product.getReferralCode())
                .uid(walletAgent.getUid())
                .build();
        walletAgentProductService.save(agentProduct);
    }

    private BigDecimal getWalletAmount(Long uid) {
        List<AccountBalanceVO> accountBalanceList = accountBalanceService.getAccountBalanceList(uid);
        List<AmountDto> amountDtoList = accountBalanceList.stream().map(accountBalanceVO ->
                new AmountDto(accountBalanceVO.getRemain(), accountBalanceVO.getCoin())).collect(Collectors.toList());
        return orderService.calDollarAmount(amountDtoList);
    }

    private BigDecimal getRechargeAmount(Long uid) {
        return orderService.amountDollarSumByChargeType(uid, ChargeType.recharge);
    }

    private BigDecimal getWithdrawAmount(Long uid) {
        return orderService.amountDollarSumByChargeType(uid, ChargeType.withdraw);
    }

    private BigDecimal getHoldAmount(Long agentId) {
        List<AmountDto> amountDtos = walletAgentMapper.holdAmountSum(agentId);
        return orderService.calDollarAmount(amountDtos);
    }

    private BigDecimal getRedemptionAmount(Long uid) {
        List<AmountDto> amountDtos = walletAgentMapper.redemptionAmountSum(uid, FundTransactionType.redemption, FundTransactionStatus.wait_audit);
        return orderService.calDollarAmount(amountDtos);
    }

    private BigDecimal getInterestAmount(Long agentId, Integer status) {
        List<AmountDto> amountDtos = walletAgentMapper.interestAmountSum(agentId, List.of(status));
        return orderService.calDollarAmount(amountDtos);
    }

    private BigDecimal getInterestAmount(Long agentId, List<Integer> status) {
        List<AmountDto> amountDtos = walletAgentMapper.interestAmountSum(agentId, status);
        return orderService.calDollarAmount(amountDtos);
    }

}
