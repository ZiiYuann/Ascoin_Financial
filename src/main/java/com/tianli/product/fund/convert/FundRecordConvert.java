package com.tianli.product.fund.convert;

import com.tianli.agent.management.vo.FundReviewVO;
import com.tianli.product.financial.entity.FinancialProduct;
import com.tianli.product.fund.entity.FundIncomeRecord;
import com.tianli.product.fund.entity.FundRecord;
import com.tianli.product.fund.entity.FundReview;
import com.tianli.product.fund.entity.FundTransactionRecord;
import com.tianli.product.fund.vo.FundIncomeRecordVO;
import com.tianli.product.fund.vo.FundProductVO;
import com.tianli.product.fund.vo.FundRecordVO;
import com.tianli.product.fund.vo.FundTransactionRecordVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FundRecordConvert {

    FundProductVO toProductVO(FinancialProduct financialProduct);

    FundRecordVO toFundVO(FundRecord fundRecord);

    FundIncomeRecordVO toFundIncomeVO(FundIncomeRecord fundIncomeRecord);

    FundTransactionRecordVO toFundTransactionVO(FundTransactionRecord fundTransactionRecord);

    FundReviewVO toReviewVO(FundReview fundReview);

}
