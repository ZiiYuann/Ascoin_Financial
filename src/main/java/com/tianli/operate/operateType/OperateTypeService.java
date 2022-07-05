package com.tianli.operate.operateType;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.operate.operateType.mapper.OperateType;
import com.tianli.operate.operateType.mapper.OperateTypeMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linyifan
 *  2/25/21 11:27 AM
 */

@Service
public class OperateTypeService extends ServiceImpl<OperateTypeMapper, OperateType> {

    /**
     * 批量新增operate_type
     * @param id    operate_id
     * @param newArrayList  同一个operate -> 多个page_location
     */
    public void saveOperateType(Long id, List<Integer> newArrayList) {
        //根据一个operate_id->多个page_location 创建多个operateType对象
        List<OperateType> operateTypes = newArrayList.stream().map(e -> {
            OperateType build = OperateType.builder()
                    .operate_id(id)
                    .page_location(e).build();
            return build;
        }).collect(Collectors.toList());
        //批量添加operateType
        boolean saveBatch = super.saveBatch(operateTypes);
        if(!saveBatch){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
    }
}
