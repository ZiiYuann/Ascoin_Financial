package com.tianli.chain.web3j;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tron.api.WalletGrpc;

/**
 * @Author cs
 * @Date 2022-01-07 11:19 上午
 */
@Configuration
public class TronConfig {

    @Bean
    public WalletGrpc.WalletBlockingStub blockingStub() {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("grpc.trongrid.io:50051").usePlaintext().build();
        return  WalletGrpc.newBlockingStub(channel);
    }
}
