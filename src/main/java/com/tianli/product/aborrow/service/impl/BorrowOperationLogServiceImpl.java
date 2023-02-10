package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.product.aborrow.entity.BorrowOperationLog;
import com.tianli.product.aborrow.mapper.BorrowOperationLogMapper;
import com.tianli.product.aborrow.service.BorrowOperationLogService;
import org.springframework.stereotype.Service;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-10
 **/
@Service
public class BorrowOperationLogServiceImpl extends ServiceImpl<BorrowOperationLogMapper, BorrowOperationLog>
        implements BorrowOperationLogService {
}
