package com.tianli.management.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-22
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinStatusQuery {

    @NotNull(message = "主键不允许为空")
    private Long id;

    private byte status;

}
