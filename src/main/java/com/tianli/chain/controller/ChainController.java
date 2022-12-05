package com.tianli.chain.controller;

import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.CoinService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.vo.CoinVO;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.exception.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-03
 **/
@RestController
@RequestMapping("/chain")
public class ChainController {

    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private CoinService coinService;
    @Resource
    private ChainConverter chainConverter;

    /**
     * 校验地址有效
     */
    @GetMapping("/valid/address")
    public Result activateWallet(NetworkType networkType, String address) {
        boolean validAddress = contractAdapter.getOne(networkType).isValidAddress(address);
        Map<String, Boolean> result = new HashMap<>();
        result.put("valid", validAddress);
        return Result.success().setData(result);
    }

    /**
     * 获取上线的币别信息
     */
    @GetMapping("/coin/infos")
    public Result coinInfos() {
        List<Coin> coins = coinService.pushCoinsWithCache();
        Map<String, List<CoinVO>> result = coins.stream()
                .map(chainConverter::toCoinVO)
                .collect(Collectors.groupingBy(CoinVO::getName));
        return Result.success().setData(result);
    }

}
