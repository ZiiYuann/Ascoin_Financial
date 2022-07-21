package com.tianli.borrow.convert;


import com.tianli.borrow.entity.BorrowCoinOrder;
import com.tianli.borrow.vo.BorrowCoinMainPageVO;
import com.tianli.borrow.vo.BorrowCoinOrderVO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BorrowConverter {

    BorrowCoinMainPageVO.BorrowOrder toMainVO(BorrowCoinOrder borrowCoinOrder);

    BorrowCoinOrderVO toVO(BorrowCoinOrder borrowCoinOrder);

}
