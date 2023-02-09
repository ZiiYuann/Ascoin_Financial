package com.tianli.product.aborrow.convert;

import com.tianli.product.aborrow.dto.BorrowRecordPledgeDto;
import com.tianli.product.aborrow.entity.BorrowConfigCoin;
import com.tianli.product.aborrow.entity.BorrowConfigPledge;
import com.tianli.product.aborrow.entity.BorrowRecordPledge;
import com.tianli.product.aborrow.query.BorrowConfigCoinIoUQuery;
import com.tianli.product.aborrow.query.BorrowConfigPledgeIoUQuery;
import com.tianli.product.aborrow.vo.MBorrowConfigCoinVO;
import com.tianli.product.aborrow.vo.MBorrowConfigPledgeVO;
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

    MBorrowConfigCoinVO toMBorrowConfigCoinVO(BorrowConfigCoin borrowConfigCoin);

    MBorrowConfigPledgeVO toMBorrowConfigPledgeVO(BorrowConfigPledge borrowConfigPledge);

    BorrowRecordPledgeDto toBorrowRecordPledgeDto(BorrowRecordPledge borrowRecordPledge);

}
