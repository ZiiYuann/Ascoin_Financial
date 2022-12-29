package com.tianli.chain.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

/**
 * @Author cs
 * @Date 2022-12-28 17:27
 */
@Data
public class DesignBtcTx {
    private Long feesRecommended;
    @SerializedName("byte")
    private Integer calcByte;
    private Long fee;
    private List<Vin> vin;
    private List<Vout> vout;

    @Data
    public static class Vin {
        private String txid;
        private Integer vout;
        private Long value;
        private String address;
        private Long block;
        private Integer used;
    }

    @Data
    public static class Vout {
        private String address;
        private Long value;
    }

}
