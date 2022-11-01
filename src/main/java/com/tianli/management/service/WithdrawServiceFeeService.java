package com.tianli.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.management.entity.WithdrawServiceFee;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.vo.WithdrawServiceFeeVO;

import java.time.LocalDate;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-27
 **/
public interface WithdrawServiceFeeService extends IService<WithdrawServiceFee> {

    /**
     * 初始化init
     */
    void init();


    /**
     * 初始化init
     */
    void init(LocalDate startTime);

    /**
     * 初始化init
     */
    void init(LocalDate startTime, LocalDate endTime);


    /**
     * 手续费展板
     */
    WithdrawServiceFeeVO board();
}
