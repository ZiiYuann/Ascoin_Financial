package com.tianli.chain.controller;

import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.chain.service.CoinService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.vo.CoinMapVO;
import com.tianli.chain.vo.CoinVO;
import com.tianli.chain.vo.NetworkTypeVO;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.exception.Result;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.tianli.common.Constants.CHAIN_TYPE_VERSION;

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
    @Resource
    private CoinBaseService coinBaseService;

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
    public Result<List<CoinMapVO>> coinInfos(Integer version) {
        final var versionFinal = Optional.ofNullable(version).orElse(0);
        List<Coin> coins = coinService.pushCoinsWithCache();

        Map<String, List<CoinVO>> coinsMap = coins.stream()
                .map(chainConverter::toCoinVO)
                .collect(Collectors.groupingBy(CoinVO::getName));

        List<CoinMapVO> result = new ArrayList<>();
        coinsMap.forEach((key, value) -> {
            List<ChainType> chainTypes = CHAIN_TYPE_VERSION.get(versionFinal);
            if (CollectionUtils.isNotEmpty(chainTypes)) {
                value = value.stream().filter(coin -> chainTypes.contains(coin.getChain())).collect(Collectors.toList());
            }
            if (CollectionUtils.isEmpty(value)) {
                return;
            }
            CoinMapVO coinMapVO = new CoinMapVO();
            coinMapVO.setName(key);
            value.sort(Comparator.comparing(coin -> coin.getChain().getSequence()));
            value.forEach(e -> e.setChainName(e.getChain().getDisplay()));
            coinMapVO.setCoins(value);
            coinMapVO.setCoinUrl(coinBaseService.getByName(key).getLogo());
            coinMapVO.setWithdrawDecimals(value.get(0).getWithdrawDecimals());
            result.add(coinMapVO);
        });

        return new Result<>(result);
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
