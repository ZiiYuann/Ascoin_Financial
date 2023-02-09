package com.tianli.product.dto;

import com.tianli.product.entity.ProductHoldRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-02
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserHoldRecordDto {

    private Long uid;

    private List<ProductHoldRecord> records;
}
