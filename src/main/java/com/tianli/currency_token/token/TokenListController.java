package com.tianli.currency_token.token;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.token.mapper.TokenList;
import com.tianli.currency_token.token.vo.TokenListVo;
import com.tianli.exception.Result;
import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.processor.CoinProcessor;
import com.tianli.exchange.vo.Mini24HrTickerVo;
import com.tianli.management.newcurrency.service.INewCurrencyManagementService;
import com.tianli.tool.BianPriceCache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/token")
public class TokenListController {

    @Resource
    INewCurrencyManagementService newCurrencyManagementService;

    @Resource
    CoinProcessor coinProcessor;

    @GetMapping("/list")
    public Result list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                       String token) {
        long count = tokenListService.count(new LambdaQueryWrapper<TokenList>()
                .like(StringUtils.isNotEmpty(token), TokenList::getToken, token)
                .ne(TokenList::getToken, CurrencyCoinEnum.usdt)
                .eq(TokenList::getActual_enable, 1)
        );
        List<TokenList> list = tokenListService.page(new Page<>(page, size), new LambdaQueryWrapper<TokenList>()
                .like(StringUtils.isNotEmpty(token), TokenList::getToken, token)
                .ne(TokenList::getToken, CurrencyCoinEnum.usdt)
                .eq(TokenList::getActual_enable, 1)
                .orderByAsc(TokenList::getSort)
        ).getRecords();
        List<TokenListVo> result = new ArrayList<>(list.size());
        for (TokenList tokenList : list) {
            TokenListVo tokenListVo = BeanUtil.copyProperties(tokenList, TokenListVo.class);
            if (tokenList.getPlatform_token()) {
                tokenListVo.setIs_platform(Boolean.TRUE);
                KLinesInfo kLinesInfo = coinProcessor.getDayKLinesInfoBySymbol(tokenListVo.getName_short() + "USDT");
                tokenListVo.setPlatform24HrTicker(Mini24HrTickerVo.getMini24HrTickerVo(kLinesInfo));
            } else {
                tokenListVo.setIs_platform(Boolean.FALSE);
                tokenListVo.setBian24HrInfo(BianPriceCache.getPrice(tokenList.getName_short() + "USDT"));
            }
            result.add(tokenListVo);
        }
        return Result.instance().setList(result, count);
    }

    @Resource
    private TokenListService tokenListService;
}
