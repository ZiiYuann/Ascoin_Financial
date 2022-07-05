package com.tianli.btc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author cs
 * @Date 2022-01-18 9:54 上午
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendDTO {
    @NotEmpty
    private List<@Valid Vin> vin = new ArrayList<>();

    @NotEmpty
    private List<@Valid Vout> vout = new ArrayList<>();

    @Min(1L)
    private Long usdt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Vin {
        @NotBlank
        private String txid;
        @NotNull
        @Range(min = 0L)
        private Integer vout;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Vout {
        @NotBlank
        private String address;
        @NotNull
        @Range(min = 546L)
        private Long value;
    }
}
