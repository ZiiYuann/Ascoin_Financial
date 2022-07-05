package com.tianli.management.salesman.entity;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.admin.AdminAndRoles;
import com.tianli.common.CommonFunction;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/6 4:10 下午
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class SalesmanUser extends Model<SalesmanUser> {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long salesman_id;

    private Long admin_id;

    private Long user_id;

    private String creator;

    private Long creator_id;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    @TableLogic
    private Boolean is_deleted;

    public static SalesmanUser collectCustomerAssignmentDto(Long userIds, Salesman salesman, AdminAndRoles my) {
        return SalesmanUser.builder()
                .id(CommonFunction.generalId())
                .salesman_id(salesman.getId())
                .user_id(userIds)
                .admin_id(salesman.getAdmin_id())
                //.creator("测试")
                //.creator_id(111L)
                .creator(my.getUsername())
                .creator_id(my.getId())
                .create_time(LocalDateTime.now())
                .is_deleted(Boolean.FALSE)
                .build();
    }
}
