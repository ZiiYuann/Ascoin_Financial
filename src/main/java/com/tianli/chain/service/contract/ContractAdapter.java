package com.tianli.chain.service.contract;

import com.tianli.common.blockchain.NetworkType;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-26
 **/
@Service
public class ContractAdapter implements BeanFactoryAware {

    private DefaultListableBeanFactory beanFactory;
    private List<ContractOperation> contractOperationList;

    public ContractOperation getOne(NetworkType networkType) {
        for (ContractOperation contractOperation : contractOperationList) {
            if(contractOperation.matchByChain(networkType)) {
                return contractOperation;
            }
        }
        throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
        contractOperationList = new ArrayList<>(this.beanFactory.getBeansOfType(ContractOperation.class).values());
    }
}
