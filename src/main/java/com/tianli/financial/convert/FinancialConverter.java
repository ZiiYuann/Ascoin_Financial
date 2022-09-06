package com.tianli.financial.convert;

import com.tianli.financial.entity.FinancialProductLadderRate;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.vo.FinancialProductDetailsVO;
import com.tianli.financial.vo.FinancialProductVO;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import com.tianli.financial.vo.IncomeByRecordIdVO;
import com.tianli.management.query.FinancialProductEditQuery;
import com.tianli.management.query.FinancialProductLadderRateIoUQuery;
import com.tianli.management.vo.ProductLadderRateVO;
import org.mapstruct.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Mapper(componentModel = "spring")
public interface FinancialConverter {

    FinancialPurchaseResultVO toFinancialPurchaseResultVO(FinancialRecord financialProductPurchaseLog);

    FinancialProductVO toFinancialProductVO(FinancialProduct financialProduct);

    FinancialProductDetailsVO toFinancialProductDetailsVO(FinancialProduct financialProduct);

    FinancialProduct toDO(FinancialProductEditQuery financialProductQuery);

    IncomeByRecordIdVO toIncomeByRecordIdVO(FinancialRecord financialRecord);

    FinancialProductLadderRate toDO(FinancialProductLadderRateIoUQuery financialProductLadderRateIoUQuery);

    ProductLadderRateVO toProductLadderRateVO(FinancialProductLadderRate rate);
}
