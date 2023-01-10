package com.tianli.chain.controller;

import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.CoinService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.vo.CoinMapVO;
import com.tianli.chain.vo.CoinVO;
import com.tianli.chain.vo.NetworkTypeVO;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.exception.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
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

        Map<String, List<CoinVO>> coinsMap = coins.stream()
                .map(chainConverter::toCoinVO)
                .collect(Collectors.groupingBy(CoinVO::getName));

        List<CoinMapVO> result = new ArrayList<>();
        coinsMap.forEach((key, value) -> {
            CoinMapVO coinMapVO = new CoinMapVO();
            coinMapVO.setName(key);
            value.sort(Comparator.comparing(coin -> coin.getChain().getSequence()));
            coinMapVO.setCoins(value);
            coinMapVO.setWithdrawDecimals(value.get(0).getWithdrawDecimals());
            result.add(coinMapVO);
        });

        return Result.success().setData(result);
    }

    @GetMapping("/network")
    public Result network() {
        List<NetworkType> list = List.of(NetworkType.values());
        final List<NetworkTypeVO> result = new ArrayList<>();

        list.stream()
                .collect(Collectors.groupingBy(NetworkType::getChainType))
                .forEach((key, value) -> {
                    NetworkTypeVO networkTypeVO = new NetworkTypeVO();
                    networkTypeVO.setChainType(key);
                    networkTypeVO.setNetworkTypes(value.stream().map(NetworkType::name).collect(Collectors.toSet()));
                    result.add(networkTypeVO);
                });

        return Result.success().setData(result);
    }

    @GetMapping("/network/type")
    public Result networkType() {
        return Result.success(List.of(NetworkType.values()));
    }
}
