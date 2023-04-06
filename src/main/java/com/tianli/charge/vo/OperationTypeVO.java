package com.tianli.charge.vo;

import com.tianli.charge.enums.OperationTypeEnum;
import lombok.Data;

/**
 * @author:yangkang
 * @create: 2023-03-16 18:52
 * @Description: 操作分类vo
 */
@Data
public class OperationTypeVO {

    private OperationTypeEnum operationType;

    private String name;

    private String nameEn;
}
