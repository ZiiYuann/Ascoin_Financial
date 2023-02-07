package com.tianli.product.aborrow.entity;

import com.tianli.product.aborrow.enums.LogType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowOperationLog {

    private Long id;

    private LogType logType;




}
