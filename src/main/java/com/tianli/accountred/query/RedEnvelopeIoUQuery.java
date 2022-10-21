package com.tianli.accountred.query;

import com.tianli.accountred.enums.RedEnvelopeType;
import com.tianli.accountred.enums.RedEnvelopeWay;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.query.IoUQuery;
import com.tianli.exception.ErrorCodeEnum;
import lombok.*;

import javax.validation.constraints.DecimalMin;
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

    private CurrencyCoin coin;

    @NotNull
    @DecimalMin(value = "0.000001" ,message = "单个红包数额不得低于0.000001")
    private BigDecimal amount;

    @Min(value = 1)
    private int num;

    private String remarks;

    private RedEnvelopeType type;

    private RedEnvelopeWay way;

    public BigDecimal getTotalAmount() {
        BigDecimal totalAmount = null;
        switch (this.getType()) {
            case NORMAL:
            case PRIVATE:
                totalAmount = this.getAmount().multiply(BigDecimal.valueOf(this.getNum()));
                break;
            case RANDOM:
                totalAmount = this.getAmount();
                break;
            default:
                ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        return totalAmount;
    }

}
