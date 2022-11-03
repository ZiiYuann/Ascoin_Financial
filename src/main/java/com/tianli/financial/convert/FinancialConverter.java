package com.tianli.financial.convert;

import com.tianli.financial.entity.FinancialProductLadderRate;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.vo.*;
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

    RecommendProductVO toRecommendProductVO(FinancialProduct financialProduct);

    CurrentProductPurchaseVO toFinancialProductDetailsVO(FinancialProductVO financialProductVO);

    FinancialProduct toDO(FinancialProductEditQuery financialProductQuery);

    RecordIncomeVO toIncomeByRecordIdVO(FinancialRecord financialRecord);

    FinancialProductLadderRate toDO(FinancialProductLadderRateIoUQuery financialProductLadderRateIoUQuery);

    ProductLadderRateVO toProductLadderRateVO(FinancialProductLadderRate rate);
}
