package com.tianli.chain.service.contract;

import com.tianli.common.blockchain.NetworkType;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-26
 **/
@Service
public class ContractAdapter {

    @Resource
    private BscTriggerContract bscTriggerContract;
    @Resource
    private EthTriggerContract ethTriggerContract;
    @Resource
    private TronTriggerContract tronTriggerContract;
    @Resource
    private TronWeb3jContract tronWeb3jContract;

    public ContractOperation getOne(NetworkType networkType) {
        switch (networkType) {
            case trc20:
                return tronTriggerContract;
            case bep20:
                return bscTriggerContract;
            case erc20:
                return ethTriggerContract;
            default:
                throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
    }

    public Web3jContractOperation getWeb3j(NetworkType networkType) {
        switch (networkType) {
            case trc20:
                return tronWeb3jContract;
            case bep20:
                return bscTriggerContract;
            case erc20:
                return ethTriggerContract;
            default:
                throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
    }
}
