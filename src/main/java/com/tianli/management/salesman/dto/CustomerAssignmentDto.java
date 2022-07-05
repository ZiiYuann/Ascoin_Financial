package com.tianli.management.salesman.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author lzy
 * @date 2022/4/7 2:44 下午
 */
@Data
public class CustomerAssignmentDto {

    @NotNull(message = "用户不能为空")
    private List<Long> userIds;

    @NotNull(message = "请指定业务员")
    private Long salesmanId;
}
