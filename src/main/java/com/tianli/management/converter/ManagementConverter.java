package com.tianli.management.converter;


import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.entity.FinancialBoardWallet;
import com.tianli.management.vo.FinancialProductBoardSummaryVO;
import com.tianli.management.vo.FinancialProductBoardVO;
import com.tianli.management.vo.FinancialWalletBoardSummaryVO;
import com.tianli.management.vo.FinancialWalletBoardVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagementConverter {

    FinancialWalletBoardVO toVO(FinancialBoardWallet financialWalletBoard);

    FinancialProductBoardVO toVO(FinancialBoardProduct FinancialProductBoard);

    FinancialProductBoardSummaryVO toFinancialProductBoardSummaryVO(FinancialBoardProduct financialBoardProduct);

    FinancialWalletBoardSummaryVO toFinancialWalletBoardSummaryVO(FinancialBoardWallet financialBoardWallet);
}
