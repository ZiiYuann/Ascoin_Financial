package com.tianli.tool.crypto.ecdsa;

/**
 * @author wangqiyun
 * @since 2022/4/9 23:30
 */
public class Secp256k1 extends ECDSA {
    @Override
    public String group() {
        return "secp256k1";
    }

    @Override
    public int length() {
        return 256;
    }

    public static Secp256k1 getInstance(){
        return Secp256k1Generate.SECP256K1;
    }

    static class Secp256k1Generate{
        public static Secp256k1 SECP256K1 = new Secp256k1();
    }
}
