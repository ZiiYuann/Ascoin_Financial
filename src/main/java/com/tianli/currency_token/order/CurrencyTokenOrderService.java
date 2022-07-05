package com.tianli.currency_token.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.dto.PurchaseTokenDTO;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.TokenOrderType;
import com.tianli.currency_token.mapper.TradeDirectionEnum;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrderMapper;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrderStatus;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CurrencyTokenOrderService extends ServiceImpl<CurrencyTokenOrderMapper, CurrencyTokenOrder> {
    @Transactional
    public void cancel(CurrencyTokenOrder currencyTokenOrder) {
        if(currencyTokenOrder.getType().equals(TokenOrderType.market)) return;
        if(currencyTokenOrder.getDirection().equals(TradeDirectionEnum.buy)){
            BigDecimal amount = currencyTokenOrder.getAmount_unit().equals(currencyTokenOrder.getToken_fiat()) ?
                    currencyTokenOrder.getAmount() : currencyTokenOrder.getAmount().multiply(currencyTokenOrder.getPrice());
            currencyTokenService.unfreeze(currencyTokenOrder.getUid(), CurrencyTypeEnum.actual, currencyTokenOrder.getToken_fiat(), amount, currencyTokenOrder.getId().toString(), CurrencyLogDes.交易);
        }
        if(currencyTokenOrder.getDirection().equals(TradeDirectionEnum.sell)){
            BigDecimal amount = currencyTokenOrder.getAmount_unit().equals(currencyTokenOrder.getToken_stock()) ?
                    currencyTokenOrder.getAmount() : currencyTokenOrder.getAmount().divide(currencyTokenOrder.getPrice(), 10, RoundingMode.HALF_UP);
            currencyTokenService.unfreeze(currencyTokenOrder.getUid(), CurrencyTypeEnum.actual, currencyTokenOrder.getToken_stock(), amount, currencyTokenOrder.getId().toString(), CurrencyLogDes.交易);
        }
        currencyTokenOrder.setStatus(CurrencyTokenOrderStatus.canceled);
        long result = currencyTokenOrderMapper.update(currencyTokenOrder, new LambdaQueryWrapper<CurrencyTokenOrder>()
                .eq(CurrencyTokenOrder::getId, currencyTokenOrder.getId())
                .eq(CurrencyTokenOrder::getStatus, CurrencyTokenOrderStatus.created)
        );
        if(result <= 0L){
            ErrorCodeEnum.CANCEL_FAIL.throwException();
        }
    }

    @Transactional
    public void freeze(Long uid, Long sn, PurchaseTokenDTO purchaseTokenDTO) {
        if(purchaseTokenDTO.getDirection().equals(TradeDirectionEnum.buy)){
            BigDecimal amount = purchaseTokenDTO.getAmount_unit().equals(purchaseTokenDTO.getFiat()) ?
                    purchaseTokenDTO.getAmount() : purchaseTokenDTO.getAmount().multiply(purchaseTokenDTO.getLimit_price());
            currencyTokenService.freeze(uid, CurrencyTypeEnum.actual, purchaseTokenDTO.getFiat(), amount, sn.toString(), CurrencyLogDes.交易);
        }
        if(purchaseTokenDTO.getDirection().equals(TradeDirectionEnum.sell)){
            BigDecimal amount = purchaseTokenDTO.getAmount_unit().equals(purchaseTokenDTO.getStock()) ?
                    purchaseTokenDTO.getAmount() : purchaseTokenDTO.getAmount().divide(purchaseTokenDTO.getLimit_price(), 10, RoundingMode.HALF_UP);
            currencyTokenService.freeze(uid, CurrencyTypeEnum.actual, purchaseTokenDTO.getStock(), amount, sn.toString(), CurrencyLogDes.交易);
        }
    }

    @Transactional
    public void tradeLimit(CurrencyTokenOrder currencyTokenOrder) {
        CurrencyCoinEnum stock = currencyTokenOrder.getToken_stock();
        CurrencyCoinEnum fiat = currencyTokenOrder.getToken_fiat();
        BigDecimal price = tokenDealService.getBianPrice(fiat, stock);

        if(!this.checkTrade(currencyTokenOrder, price)) return;

        BigDecimal fiat_amount = BigDecimal.ZERO;
        BigDecimal stock_amount = BigDecimal.ZERO;

        if(currencyTokenOrder.getToken_fiat().equals(currencyTokenOrder.getAmount_unit())){
            fiat_amount = currencyTokenOrder.getAmount();
            stock_amount = currencyTokenOrder.getAmount().divide(price, 10, RoundingMode.HALF_UP);
        }
        if(currencyTokenOrder.getToken_stock().equals(currencyTokenOrder.getAmount_unit())){
            fiat_amount = currencyTokenOrder.getAmount().multiply(price);
            stock_amount = currencyTokenOrder.getAmount();
        }

        switch (currencyTokenOrder.getDirection()){
            case buy:
                tokenDealService.limitBuy(currencyTokenOrder.getUid(), fiat, stock, fiat_amount, stock_amount, price, currencyTokenOrder.getId());
                break;
            case sell:
                tokenDealService.limitSell(currencyTokenOrder.getUid(), fiat, stock, fiat_amount, stock_amount, price, currencyTokenOrder.getId());
                break;
        }
        //
        currencyTokenOrder.setDeal_price(price);
        currencyTokenOrder.setStatus(CurrencyTokenOrderStatus.success);
        currencyTokenOrder.setDeal_time(requestInitService.now());
        if(currencyTokenOrder.getToken_stock().equals(currencyTokenOrder.getAmount_unit())){
            currencyTokenOrder.setDeal_amount(stock_amount);
        }
        if(currencyTokenOrder.getToken_fiat().equals(currencyTokenOrder.getAmount_unit())){
            currencyTokenOrder.setDeal_amount(fiat_amount);
        }
        long result = currencyTokenOrderMapper.update(currencyTokenOrder,
                new LambdaQueryWrapper<CurrencyTokenOrder>().eq(CurrencyTokenOrder::getStatus, CurrencyTokenOrderStatus.created)
                .eq(CurrencyTokenOrder::getId, currencyTokenOrder.getId())
        );
        if(result <= 0L){
            ErrorCodeEnum.TRADE_FAIL.throwException();
        }
    }


    @Transactional
    public void tradeMarket(CurrencyTokenOrder currencyTokenOrder) {
        CurrencyCoinEnum stock = currencyTokenOrder.getToken_stock();
        CurrencyCoinEnum fiat = currencyTokenOrder.getToken_fiat();
        BigDecimal price = tokenDealService.getBianPrice(fiat, stock);

        if(!this.checkTrade(currencyTokenOrder, price)) return;

        BigDecimal fiat_amount = BigDecimal.ZERO;
        BigDecimal stock_amount = BigDecimal.ZERO;

        if(currencyTokenOrder.getToken_fiat().equals(currencyTokenOrder.getAmount_unit())){
            fiat_amount = currencyTokenOrder.getAmount();
            stock_amount = currencyTokenOrder.getAmount().divide(price, 10, RoundingMode.HALF_UP);
        }
        if(currencyTokenOrder.getToken_stock().equals(currencyTokenOrder.getAmount_unit())){
            fiat_amount = currencyTokenOrder.getAmount().multiply(price);
            stock_amount = currencyTokenOrder.getAmount();
        }

        switch (currencyTokenOrder.getDirection()){
            case buy:
                tokenDealService.marketBuy(currencyTokenOrder.getUid(), fiat, stock, fiat_amount, stock_amount, price, currencyTokenOrder.getId());
                break;
            case sell:
                tokenDealService.marketSell(currencyTokenOrder.getUid(), fiat, stock, fiat_amount, stock_amount, price, currencyTokenOrder.getId());
                break;
        }
        currencyTokenOrder.setDeal_price(price);
        currencyTokenOrder.setStatus(CurrencyTokenOrderStatus.success);
        currencyTokenOrder.setDeal_time(requestInitService.now());
        if(currencyTokenOrder.getToken_stock().equals(currencyTokenOrder.getAmount_unit())){
            currencyTokenOrder.setDeal_amount(stock_amount);
        }
        if(currencyTokenOrder.getToken_fiat().equals(currencyTokenOrder.getAmount_unit())){
            currencyTokenOrder.setDeal_amount(fiat_amount);
        }
        long result = currencyTokenOrderMapper.update(currencyTokenOrder,
                new LambdaQueryWrapper<CurrencyTokenOrder>().eq(CurrencyTokenOrder::getStatus, CurrencyTokenOrderStatus.created)
                        .eq(CurrencyTokenOrder::getId, currencyTokenOrder.getId())
        );
        if(result <= 0L){
            ErrorCodeEnum.TRADE_FAIL.throwException();
        }
    }

    public boolean checkTrade(CurrencyTokenOrder currencyTokenOrder, BigDecimal price) {
        boolean tag = false;
        switch (currencyTokenOrder.getType()){
            case market:
                tag = true;
                break;
            case limit:
                if(currencyTokenOrder.getDirection().equals(TradeDirectionEnum.buy) && currencyTokenOrder.getPrice().compareTo(price) >= 0) {
                    tag = true;
                }
                if(currencyTokenOrder.getDirection().equals(TradeDirectionEnum.sell) && currencyTokenOrder.getPrice().compareTo(price) <= 0) {
                    tag = true;
                }
                break;
        }
        return tag;
    }


    @Resource
    private TokenDealService tokenDealService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private CurrencyTokenOrderMapper currencyTokenOrderMapper;
    @Resource
    private CurrencyTokenService currencyTokenService;
}
