package com.tianli.management.directioanalconfig.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.directioanalconfig.dto.CreateDirectionalConfigDTO;
import com.tianli.management.directioanalconfig.mapper.DirectionalConfig;
import com.tianli.management.directioanalconfig.mapper.DirectionalConfigMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author chensong
 *  2021-03-04 11:05
 * @since 1.0.0
 */
@Service
public class DirectionalConfigService extends ServiceImpl<DirectionalConfigMapper, DirectionalConfig> {

    public void create(CreateDirectionalConfigDTO dto) {
        AdminInfo adminInfo = AdminContent.get();
        if(Objects.isNull(adminInfo)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        List<DirectionalConfig> list = this.list(new LambdaQueryWrapper<DirectionalConfig>()
                .eq(DirectionalConfig::getUid, dto.getUid())
                .eq(DirectionalConfig::getCurrency_type,dto.getCurrency_type())
                .and(e -> e.between(DirectionalConfig::getStart_time,dto.getStart_time(),dto.getEnd_time()).or()
                        .between(DirectionalConfig::getEnd_time,dto.getStart_time(),dto.getEnd_time())));
        if(list.size()>0){
            ErrorCodeEnum.TIME_CONFLICT.throwException();
        }
        DirectionalConfig build = DirectionalConfig.builder()
                .id(CommonFunction.generalId())
                .uid(dto.getUid())
                .currency_type(dto.getCurrency_type())
                .result(dto.getResult())
                .start_time(dto.getStart_time())
                .end_time(dto.getEnd_time())
                .admin_id(adminInfo.getAid())
                .admin_username(adminInfo.getUsername())
                .create_time(LocalDateTime.now())
                .remark(dto.getRemark()).build();
        if(!this.save(build)) ErrorCodeEnum.SYSTEM_BUSY.throwException();
    }
}
