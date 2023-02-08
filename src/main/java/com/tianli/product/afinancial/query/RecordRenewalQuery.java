package com.tianli.product.afinancial.query;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-28
 **/
@Data
public class RecordRenewalQuery {

    @NotNull
    private Long recordId;

    @NotNull
    private boolean autoRenewal;
}
