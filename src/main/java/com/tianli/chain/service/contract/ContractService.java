package com.tianli.chain.service.contract;

import com.tianli.common.blockchain.NetworkType;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-26
 **/
@Component
public abstract class ContractService {

    @Resource
    private BscTriggerContract bscTriggerContract;
    @Resource
    private EthTriggerContract ethTriggerContract;
    @Resource
    private TronTriggerContract tronTriggerContract;


    public abstract String computeAddress(long uid) throws IOException;

    public abstract String computeAddress(BigInteger uid) throws IOException;

    public abstract String computeAddress(String walletAddress, BigInteger uid) throws IOException;

    /**
     * 归集接口
     * @param toAddress 归集地址 如果为null，修改为主钱包地址
     * @param uids Address表中的id
     * @param addresses 归集地址列表
     * @return 返回交易hash
     */
    public abstract String recycle(String toAddress, List<Long> uids, List<String> addresses);

    public ContractService getOne(NetworkType networkType) {
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

}
