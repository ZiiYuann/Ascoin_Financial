package com.tianli.management.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.management.entity.ServiceFee;
import com.tianli.management.query.TimeQuery;
import com.tianli.management.vo.ServiceFeeVO;

import java.time.LocalDate;

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
    void init(LocalDate startTime);

    /**
     * 初始化init
     */
    void init(LocalDate startTime, LocalDate endTime);


    /**
     * 手续费展板
     */
    ServiceFeeVO board(TimeQuery timeQuery, byte type);
}
