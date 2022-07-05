package com.tianli.management.spot.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.management.spot.dao.SGChargeMapper;
import com.tianli.management.spot.dao.SGRechargeMapper;
import com.tianli.management.spot.vo.SGRechargeByTypeVo;
import com.tianli.management.spot.vo.SGRechargeListVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author lzy
 * @date 2022/4/24 14:07
 */
@Service
@Slf4j
public class SGRechargeService {

    @Resource
    SGRechargeMapper sgRechargeMapper;
    @Resource
    TokenContractService tokenContractService;

    public IPage<SGRechargeListVo> page(String username, String token, String startTime, String endTime, String txid, Long salesman_id, Integer page, Integer size) {
        Long count = sgRechargeMapper.selectCount(username, token, startTime, endTime, txid, salesman_id);
        if (ObjectUtil.isNull(count) || count <= 0) {
            return new Page<>(page, size);
        }
        List<SGRechargeListVo> sgRechargeListVoList = sgRechargeMapper.selectPage(username, token, startTime, endTime, txid, salesman_id, (page - 1) * size, size);
        return new Page<SGRechargeListVo>(page, size).setRecords(sgRechargeListVoList).setTotal(count);
    }

    public BigDecimal sumUAmount(String username, String token, String startTime, String endTime, String txid, Long salesman_id) {
        return sgRechargeMapper.selectSumAmount(username, token, startTime, endTime, txid, salesman_id);
    }

    public IPage<SGRechargeByTypeVo> listSumAmount(Integer tokenId, String currencyType, Integer page, Integer size) {
        TokenContract tokenContract=TokenContract.builder().build();
        if(!Objects.isNull(tokenId)){
            tokenContract = tokenContractService.getById(tokenId);
        }
        String token = Objects.isNull(tokenContract.getToken())?null:tokenContract.getToken().name();
        String chain = Objects.isNull(tokenContract.getChain())?null:tokenContract.getChain().name();
        Long count = sgRechargeMapper.listSumAmountCount(token, chain,currencyType);
        if (ObjectUtil.isNull(count) || count <= 0) {
            return new Page<>(page, size);
        }
        List<SGRechargeByTypeVo> list = sgRechargeMapper.listSumAmount(token,chain, currencyType, (page-1)*size, size);
        return new Page<SGRechargeByTypeVo>(page, size).setRecords(list).setTotal(count);
    }
}
