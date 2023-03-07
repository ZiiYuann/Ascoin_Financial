package com.tianli.chain.service.contract;

import com.tianli.address.service.AddressService;
import com.tianli.chain.entity.Coin;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-26
 **/
@Service
public class ContractAdapter implements BeanFactoryAware {

    @Resource
    private List<ContractOperation> contractOperationList;
    @Resource
    private List<Web3jContractOperation> web3jContractOperations;
    @Resource
    private AddressService addressService;

    // 不包括tron 的web3j
    public ContractOperation getOne(NetworkType networkType) {
        for (ContractOperation contractOperation : contractOperationList) {
            if (contractOperation.matchByChain(networkType)) {
                if (contractOperation instanceof TronWeb3jContract) {
                    continue;
                }
                return contractOperation;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }

    public Web3jContractOperation getWeb3jOne(NetworkType networkType) {
        for (Web3jContractOperation web3jContractOperation : web3jContractOperations) {
            if (web3jContractOperation.matchByChain(networkType)) {
                return web3jContractOperation;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }

    @Override
    public void setBeanFactory(@Nonnull BeanFactory beanFactory) throws BeansException {
        var defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;
        contractOperationList = new ArrayList<>(defaultListableBeanFactory.getBeansOfType(ContractOperation.class).values());
    }

    /**
     * 获取对应地址的余额信息
     *
     * @param networkType 网络信息
     * @param address     地址
     * @param coin        币别
     * @return 余额
     */
    public BigDecimal getBalance(NetworkType networkType, String address, Coin coin) {
        var baseContractService = this.getOne(networkType);
        var balance = coin.isMainToken() ? baseContractService.mainBalance(address) :
                baseContractService.tokenBalance(address, coin);
        balance = TokenAdapter.alignment(coin, balance);
        return balance;
    }

    /**
     * 获取热钱包对应的余额信息
     *
     * @param networkType 网络信息
     * @param coin        币别
     * @return 余额
     */
    public BigDecimal getBalance(NetworkType networkType, Coin coin) {
        String address = addressService.getAddress(networkType.getChainType());
        return getBalance(networkType, address, coin);
    }
}
