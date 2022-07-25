package com.tianli.borrow.convert;


import com.tianli.borrow.bo.BorrowOrderConfigBO;
import com.tianli.borrow.entity.BorrowCoinConfig;
import com.tianli.borrow.vo.BorrowCoinConfigVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BorrowCoinConfigConverter {

    BorrowCoinConfigVO toVO(BorrowCoinConfig borrowCoinConfig);

    BorrowCoinConfig toDO(BorrowOrderConfigBO bo);

}
