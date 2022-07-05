package com.tianli.management.recycle;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.address.mapper.Address;
import com.tianli.address.mapper.AddressMapper;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.BscTriggerContract;
import com.tianli.common.blockchain.EthTriggerContract;
import com.tianli.common.blockchain.TronTriggerContract;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.loan.dao.LoanAddressMapper;
import com.tianli.loan.entity.LoanAddress;
import com.tianli.management.recycle.record.RecycleRecord;
import com.tianli.management.recycle.record.RecycleRecordService;
import com.tianli.tool.MapTool;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author cs
 * @Date 2022-03-28 10:13 上午
 */
@Service
public class RecycleService {

    @Resource
    private TronTriggerContract tronTriggerContract;
    @Resource
    private BscTriggerContract bscTriggerContract;
    @Resource
    private EthTriggerContract ethTriggerContract;
    @Resource
    private AddressMapper addressMapper;

    @Resource
    private LoanAddressMapper loanAddressMapper;

    @Resource
    TokenContractService tokenContractService;

    @Resource
    RecycleRecordService recycleRecordService;

    public final static String USDT_TRC20_CONTRACT = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
    public final static String USDT_BEP20_CONTRACT = "0x55d398326f99059fF775485246999027B3197955";
    public final static String USDT_ERC20_CONTRACT = "0xdAC17F958D2ee523a2206206994597C13D831ec7";
    public final static String USDC_TRC20_CONTRACT = "TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8";
    public final static String USDC_BEP20_CONTRACT = "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d";
    public final static String USDC_ERC20_CONTRACT = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48";

    public String execute(RecycleDTO dto) {
        List<Long> uids = dto.getUids();
        TokenContract tokenContract = tokenContractService.getById(dto.getTokenId());
        if (ObjectUtil.isNull(tokenContract)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        if (CollectionUtils.isEmpty(uids)) return null;
        List<Address> addresses = addressMapper.selectList(Wrappers.lambdaQuery(Address.class).in(Address::getId, uids));
        //查询用户还款地址表
        List<LoanAddress> loanAddresses = loanAddressMapper.selectList(Wrappers.lambdaQuery(LoanAddress.class).in(LoanAddress::getId, uids));
        List<Address> collect = loanAddresses.stream().map(e -> {
            Address loanAddressToAddress = new Address();
            BeanUtils.copyProperties(e, loanAddressToAddress);
            return loanAddressToAddress;
        }).collect(Collectors.toList());
        addresses.addAll(collect);//用户还款地址表和用户取款地址表合并

        String contractAddress = tokenContract.getContract_address();
        uids = addresses.stream().filter(e -> {
            switch (tokenContract.getChain()) {
                case trc20: {
                    if (tokenContract.getToken().equals(CurrencyCoinEnum.trx))
                        return tronTriggerContract.trxBalance(e.getTron()).compareTo(BigDecimal.ZERO) > 0;
                    return tronTriggerContract.trc20Balance(e.getTron(), contractAddress).compareTo(BigDecimal.ZERO) > 0;
                }
                case bep20: {
                    if (tokenContract.getToken().equals(CurrencyCoinEnum.bnb))
                        return bscTriggerContract.bnbBalance(e.getBsc()).compareTo(BigInteger.ZERO) > 0;
                    return bscTriggerContract.bep20Balance(e.getBsc(), contractAddress).compareTo(BigInteger.ZERO) > 0;
                }
                case erc20: {
                    if (tokenContract.getToken().equals(CurrencyCoinEnum.eth))
                        return ethTriggerContract.ethBalance(e.getEth()).compareTo(BigInteger.ZERO) > 0;
                    return ethTriggerContract.erc20Balance(e.getEth(), contractAddress).compareTo(BigInteger.ZERO) > 0;
                }
            }
            return false;
        }).map(Address::getId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(uids)) return null;
        String txHash = null;
        switch (tokenContract.getChain()) {
            case trc20: {
                if (tokenContract.getToken().equals(CurrencyCoinEnum.trx)) {
                    txHash = tronTriggerContract.recycle(null, uids, new ArrayList<>());
                    break;
                }
                txHash = tronTriggerContract.recycle(null, uids, List.of(contractAddress));
                break;
            }
            case bep20: {
                if (tokenContract.getToken().equals(CurrencyCoinEnum.bnb)) {
                    txHash = bscTriggerContract.recycle(null, uids, new ArrayList<>());
                    break;
                }
                txHash = bscTriggerContract.recycle(null, uids, List.of(contractAddress));
                break;
            }
            case erc20: {
                if (tokenContract.getToken().equals(CurrencyCoinEnum.eth)) {
                    txHash = ethTriggerContract.recycle(null, uids, new ArrayList<>());
                    break;
                }
                txHash = ethTriggerContract.recycle(null, uids, List.of(contractAddress));
                break;
            }

        }
        if (StrUtil.isNotBlank(txHash)) {
            recycleRecordService.save(RecycleRecord.builder()
                    .id(CommonFunction.generalId())
                    .tx_hash(txHash)
                    .token(tokenContract.getToken())
                    .chain_type(tokenContract.getChain())
                    .create_time(LocalDateTime.now())
                    .process(Boolean.FALSE)
                    .main_currency(tokenContract.isMainCurrency())
                    .build());
        }
        return txHash;
    }

    public Map balance(String address, Integer tokenId, Integer page, Integer size) {
        List<Address> addresses = addressMapper.selectList(Wrappers.lambdaQuery(Address.class)
                .and(StringUtils.isNotBlank(address), e -> e.like(Address::getTron, address).or().like(Address::getBsc, address).or().like(Address::getEth, address))
                .eq(Address::getType, CurrencyTypeEnum.normal)
        );
        //查询用户还款地址表
        List<LoanAddress> loanAddresses = loanAddressMapper.selectList(Wrappers.lambdaQuery(LoanAddress.class)
                        .and(StringUtils.isNotBlank(address),
                                e -> e.like(LoanAddress::getTron, address).
                                        or().like(LoanAddress::getBsc, address).
                                        or().like(LoanAddress::getEth, address))
        );
        List<Address> collect = loanAddresses.stream().map(e -> {
            Address loanAddressToAddress = new Address();
            BeanUtils.copyProperties(e, loanAddressToAddress);
            return loanAddressToAddress;
        }).collect(Collectors.toList());
        addresses.addAll(collect);//用户还款地址表和用户取款地址表合并
        List < RecycleBalanceVO > vos = new ArrayList<>();
        List<RecycleBalanceVO> bep20 = new ArrayList<>();
        List<RecycleBalanceVO> erc20 = new ArrayList<>();
        List<RecycleBalanceVO> trc20 = new ArrayList<>();
        TokenContract tokenContract = tokenContractService.getById(tokenId);
        if (ObjectUtil.isNull(tokenContract)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        switch (tokenContract.getChain()) {
            case bep20:
                bep20 = getBep20(addresses, tokenContract);
                break;
            case erc20:
                erc20 = getErc20(addresses, tokenContract);
                break;
            case trc20:
                trc20 = getTrc20(addresses, tokenContract);
                break;
        }
        vos.addAll(bep20);
        vos.addAll(erc20);
        vos.addAll(trc20);
        return MapTool.Map().put("total", vos.size()).put("list", vos.subList(Math.min(vos.size(), (page - 1) * size), Math.min(vos.size(), page * size)));
    }

    private List<RecycleBalanceVO> getTrc20(List<Address> addresses, TokenContract tokenContract) {
        List<RecycleBalanceVO> trc20 = addresses.stream()
                .map(e -> RecycleBalanceVO.builder().id(e.getId()).address(e.getTron())
                        .balance(
                                tokenContract.getToken().equals(CurrencyCoinEnum.trx) ? tokenContract.money(tronTriggerContract.trxBalance(e.getTron()).toBigInteger()) :
                                        tokenContract.money(tronTriggerContract.trc20Balance(e.getTron(), tokenContract.getContract_address()).toBigInteger())
                        )
                        .token(tokenContract.getToken()).chain(tokenContract.getChain()).build()
                ).filter(e -> e.getBalance().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
        return trc20;
    }


    private List<RecycleBalanceVO> getErc20(List<Address> addresses, TokenContract tokenContract) {
        List<RecycleBalanceVO> erc20 = addresses.stream().filter(e -> e.getEth() != null)
                .map(e -> RecycleBalanceVO.builder().id(e.getId()).address(e.getEth())
                        .balance(
                                tokenContract.getToken().equals(CurrencyCoinEnum.eth) ? tokenContract.money(ethTriggerContract.ethBalance(e.getEth())) :
                                        tokenContract.money(ethTriggerContract.erc20Balance(e.getEth(), tokenContract.getContract_address()))
                        )
                        .token(tokenContract.getToken()).chain(tokenContract.getChain()).build()
                ).filter(e -> e.getBalance().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
        return erc20;
    }


    private List<RecycleBalanceVO> getBep20(List<Address> addresses, TokenContract tokenContract) {
        List<RecycleBalanceVO> bep20 = addresses.stream()
                .map(e -> RecycleBalanceVO.builder().id(e.getId()).address(e.getBsc())
                        .balance(
                                tokenContract.getToken().equals(CurrencyCoinEnum.bnb) ? tokenContract.money(bscTriggerContract.bnbBalance(e.getBsc())) :
                                        tokenContract.money(bscTriggerContract.bep20Balance(e.getBsc(), tokenContract.getContract_address()))
                        )
                        .token(tokenContract.getToken()).chain(tokenContract.getChain()).build()
                ).filter(e -> e.getBalance().compareTo(BigDecimal.ZERO) > 0).collect(Collectors.toList());
        return bep20;
    }

}
