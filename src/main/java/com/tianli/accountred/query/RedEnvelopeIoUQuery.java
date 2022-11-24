package com.tianli.accountred.query;

import com.tianli.accountred.enums.RedEnvelopeType;
import com.tianli.accountred.enums.RedEnvelopeWay;
import com.tianli.common.query.IoUQuery;
import com.tianli.exception.ErrorCodeEnum;
import lombok.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeIoUQuery extends IoUQuery {

    @NotBlank(message = "红包唯一标示不允许为空")
    private String flag;

    private String coin;

    @NotNull
    private BigDecimal amount;

    @Min(value = 1)
    private int num;

    private String remarks;

    private RedEnvelopeType type;

    private RedEnvelopeWay way;

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = null;
        BigDecimal limitAmount = new BigDecimal("0.000001");
        switch (this.getType()) {
            case NORMAL:
            case PRIVATE:
                if (limitAmount.compareTo(this.getAmount()) > 0) {
                    ErrorCodeEnum.RED_LIMIT_AMOUNT.throwException();
                }

                totalAmount = this.getAmount().multiply(BigDecimal.valueOf(this.getNum()));
                break;
            case RANDOM:
                if (limitAmount.multiply(BigDecimal.valueOf(num)).compareTo(this.getAmount()) > 0) {
                    ErrorCodeEnum.RED_LIMIT_AMOUNT.throwException();
                }

                totalAmount = this.getAmount();
                break;
            default:
                ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        return totalAmount;
    }

}
