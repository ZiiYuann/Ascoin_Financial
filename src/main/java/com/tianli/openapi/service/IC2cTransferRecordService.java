package com.tianli.openapi.service;

import com.tianli.openapi.dto.IdDto;
import com.tianli.openapi.entity.C2cTransferRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.openapi.query.OpenapiC2CQuery;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author xianeng
 * @since 2023-04-12
 */
public interface IC2cTransferRecordService extends IService<C2cTransferRecord> {

   IdDto c2cTransfer(OpenapiC2CQuery query);
}
