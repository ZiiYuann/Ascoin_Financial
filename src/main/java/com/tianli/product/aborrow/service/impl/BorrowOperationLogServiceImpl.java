package com.tianli.product.aborrow.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.common.QueryWrapperUtils;
import com.tianli.product.aborrow.convert.BorrowConvert;
import com.tianli.product.aborrow.entity.BorrowOperationLog;
import com.tianli.product.aborrow.mapper.BorrowOperationLogMapper;
import com.tianli.product.aborrow.query.BorrowOperationLogQuery;
import com.tianli.product.aborrow.service.BorrowOperationLogService;
import com.tianli.product.aborrow.vo.BorrowOperationLogVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-10
 **/
@Service
public class BorrowOperationLogServiceImpl extends ServiceImpl<BorrowOperationLogMapper, BorrowOperationLog>
        implements BorrowOperationLogService {

    @Resource
    private BorrowConvert borrowConvert;
    @Resource
    private CoinBaseService coinBaseService;

    @Override
    public IPage<BorrowOperationLogVO> logs(Page<BorrowOperationLog> page, BorrowOperationLogQuery query) {
        return this.page(page, QueryWrapperUtils.generate(BorrowOperationLog.class, query))
                .convert(record -> {
                    BorrowOperationLogVO borrowOperationLogVO = borrowConvert.toBorrowOperationLogVO(record);
                    borrowOperationLogVO.setLogo(coinBaseService.getByName(record.getCoin()).getLogo());
                    return borrowOperationLogVO;
                });
    }
}
