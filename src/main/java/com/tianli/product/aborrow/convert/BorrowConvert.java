package com.tianli.product.aborrow.convert;

import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.entity.*;
import com.tianli.product.aborrow.query.BorrowConfigCoinIoUQuery;
import com.tianli.product.aborrow.query.BorrowConfigPledgeIoUQuery;
import com.tianli.product.aborrow.vo.*;
import com.tianli.product.afinancial.entity.FinancialRecord;
import org.mapstruct.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
@Mapper(componentModel = "spring")
public interface BorrowConvert {

    BorrowConfigCoin toBorrowConfigCoin(BorrowConfigCoinIoUQuery query);

    BorrowConfigPledge toBorrowConfigPledge(BorrowConfigPledgeIoUQuery query);

    BorrowConfigPledgeVO toBorrowConfigPledgeVO(BorrowConfigPledge borrowConfigPledge);

    BorrowConfigCoinVO toBorrowConfigCoinVO(BorrowConfigCoin borrowConfigCoin);

    MBorrowConfigCoinVO toMBorrowConfigCoinVO(BorrowConfigCoin borrowConfigCoin);

    MBorrowConfigPledgeVO toMBorrowConfigPledgeVO(BorrowConfigPledge borrowConfigPledge);

    BorrowRecordPledgeDto toBorrowRecordPledgeDto(BorrowRecordPledge borrowRecordPledge);

    MBorrowUserVO toMBorrowUserVO(BorrowRecord borrowRecord);

    MBorrowOperationLogVO toMBorrowOperationLogVO(BorrowOperationLog borrowOperationLog);

    BorrowRecordVO toBorrowRecordVO(BorrowRecord borrowRecord);

    BorrowOperationLogVO toBorrowOperationLogVO(BorrowOperationLog borrowOperationLog);

    ProductPledgeVO toProductPledgeVO(FinancialRecord financialRecord);

    AccountBorrowVO toAccountBorrowVO(BorrowConfigCoinVO borrowConfigCoin);
}
