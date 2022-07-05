package com.tianli.currency_token.token;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.token.mapper.TokeListMapper;
import com.tianli.currency_token.token.mapper.TokenList;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class TokenListService extends ServiceImpl<TokeListMapper, TokenList> {

    public TokenList getByToken(CurrencyCoinEnum token) {
        BoundValueOperations<String, String> tokenInfoOps = redisTemplate.boundValueOps(token.name() + ".info");
        String str = tokenInfoOps.get();
        TokenList tokenList = null;
        if(str == null){
            tokenList = tokeListMapper.selectOne(new LambdaQueryWrapper<TokenList>().eq(TokenList::getToken, token));
            tokenInfoOps.set(new Gson().toJson(tokenList), 5, TimeUnit.MINUTES);
        } else {
            tokenList = new Gson().fromJson(str, TokenList.class);
        }
        return tokenList;
    }

    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private TokeListMapper tokeListMapper;
}
