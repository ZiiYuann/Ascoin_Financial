package com.tianli.btc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author cs
 * @Date 2022-01-18 9:53 上午
 */
@Data
public class RawTransaction {
    private String hex;
    private String txid;
    private Integer size;
    private Integer confirmations;
    private String blockhash;
    private Long time;
    private List<Vin> vin = new ArrayList<>();
    private List<Vout> vout = new ArrayList<>();


    @Data
    public static class Vin {
        private String txid;
        private Integer vout;
    }

    @Data
    public static class Vout {
        private String value;
        private Integer n;
        private ScriptPubKey scriptPubKey;

        @Data
        public static class ScriptPubKey {
            private String asm;
            private String hex;
            private List<String> addresses = new ArrayList<>();
        }
    }
}
