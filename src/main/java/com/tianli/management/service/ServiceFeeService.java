package com.tianli.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.management.entity.ServiceFee;
import com.tianli.management.query.TimeQuery;
import com.tianli.management.vo.BoardServiceFeeVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-27
 **/
public interface ServiceFeeService extends IService<ServiceFee> {

    /**
     * 初始化init
     */
    void init();


    /**
     * 初始化init
     */
    void init(LocalDate startTime, Byte type);

    /**
     * 初始化init
     */
    void init(LocalDate startTime, LocalDate endTime, Byte type);


    /**
     * 手续费展板
     */
    BoardServiceFeeVO board(TimeQuery timeQuery, Byte type);

    BigDecimal serviceFee(Byte type, LocalDateTime startTime, LocalDateTime endTime);
}
