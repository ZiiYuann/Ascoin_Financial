package com.tianli.chain.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LineTransactionDetail {
    private String code;
    private String msg;
    private long time = System.currentTimeMillis();
    private Data data;

    @lombok.Data
    public static class Data{
        private DetailBTC btc;
        private DetailETH eth;
        private DetailETH trx;
    }

    @lombok.Data
    public static class DetailBTC{
        private double fee_conversion;
        private String extend_url;
        private BigInteger fee;
    }

    @lombok.Data
    public static class DetailETH{
        private long block;
        private String extend_url;
        private BigInteger fee;
    }

    @lombok.Data
    public static class DetailTRX{
        private long block;
        private String extend_url;
        private BigInteger fee;
    }
}


/*
{
    "code": "0",
    "msg": "成功",
    "time": 1613800385210,
    "data": {
        "btc": {
            "fee_conversion": 0.00122771,
            "extend_url": "https://www.blockchain.com/btc/tx/29b18b00fee1a6871acf1c75972349e3150a0bc449642d99a06772a0e3db686d",
            "fee": "122771",
            "vin": [
                {
                    "address": "17A16QmavnUfCW11DAApiJxp7ARnxN5pGX",
                    "value_conversion": "202.31198002",
                    "value": 20231198002
                }
            ],
            "vout": [
                {
                    "address": "bc1qf0nd0hmdfggypgme67pz7dh670zfzsn9yj72pa",
                    "value_conversion": "0.00848762",
                    "value": 848762
                },
                {
                    "address": "393yefvCRfTN68hJ6Vdou48EAwXdpVPQTo",
                    "value_conversion": "0.04724512",
                    "value": 4724512
                },
                {
                    "address": "39sG3qpCYMCV9j1Y1yzf7zCzABYX5nHj3h",
                    "value_conversion": "0.11950000",
                    "value": 11950000
                },
                {
                    "address": "17A16QmavnUfCW11DAApiJxp7ARnxN5pGX",
                    "value_conversion": "202.13551957",
                    "value": 20213551957
                }
            ]
        },
        "ltc": { }
    }
}


{
    "code": "0",
    "msg": "成功",
    "time": 1613800320192,
    "data": {
        "btc": { },
        "eth": {
            "hash": "0x8b656a48bf8ec0611c06299ee52e022fce0bff6d5e0a15bb814875abaddc06c3",
            "nonce": 22,
            "block": 11650679,
            "from_address": "0x5b46D5CA2839cA9caB3B0f1E35c967aeFF709Ff3",
            "to_address": "0x99331BDc872bEd3FD67bD320b379fD38ab2f1aB7",
            "value": "0",
            "gas_price": 54000000000,
            "create_time": "2021-01-14 10:50:21",
            "gas_used": 299628,
            "extend_url": "https://cn.etherscan.com/tx/0x8b656a48bf8ec0611c06299ee52e022fce0bff6d5e0a15bb814875abaddc06c3",
            "fee": 16179912000000000
        },
        "ltc": { }
    }
}
*/
