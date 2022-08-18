package com.tianli.management.converter;


import com.tianli.financial.entity.FinancialProduct;
import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.entity.FinancialBoardWallet;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.management.query.HotWalletDetailedIoUQuery;
import com.tianli.management.vo.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagementConverter {

    FinancialWalletBoardVO toVO(FinancialBoardWallet financialWalletBoard);

    FinancialProductBoardVO toVO(FinancialBoardProduct FinancialProductBoard);

    FinancialProductBoardSummaryVO toFinancialProductBoardSummaryVO(FinancialBoardProduct financialBoardProduct);

    FinancialWalletBoardSummaryVO toFinancialWalletBoardSummaryVO(FinancialBoardWallet financialBoardWallet);

    MFinancialProductVO toMFinancialProductVO(FinancialProduct financialProduct);

    HotWalletDetailed toDO(HotWalletDetailedIoUQuery HotWalletDetailed);

    HotWalletDetailedVO toHotWalletDetailedVO(HotWalletDetailed hotWalletDetailed);
}
