package com.tianli.product.aborrow.query;

import com.baomidou.mybatisplus.core.enums.SqlKeyword;
import com.tianli.common.annotation.QueryWrapperGenerator;
import com.tianli.product.aborrow.enums.BorrowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-07
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowQuery {

    @QueryWrapperGenerator(field = "coin", op = SqlKeyword.LIKE)
    private String coinStr;

    @QueryWrapperGenerator(field = "coin")
    private String coin;

    @QueryWrapperGenerator(field = "status")
    private Integer status;

    private BorrowStatus borrowStatus;

    @QueryWrapperGenerator(field = "weight", op = SqlKeyword.DESC)
    private Boolean orderByWeight;

    public void setBorrowStatus(BorrowStatus borrowStatus) {
        this.borrowStatus = borrowStatus;
        if (Objects.nonNull(borrowStatus)) {
            status = borrowStatus.getStatus();
        }
    }
}
