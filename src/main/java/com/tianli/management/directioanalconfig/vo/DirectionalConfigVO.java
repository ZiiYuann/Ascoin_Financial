package com.tianli.management.directioanalconfig.vo;

import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.management.directioanalconfig.dto.DirectionalConfigStatus;
import com.tianli.management.directioanalconfig.mapper.DirectionalConfig;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chensong
 *  2021-03-04 14:10
 * @since 1.0.0
 */
@Data
@Builder
public class DirectionalConfigVO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 状态
     */
    private DirectionalConfigStatus status;

    /**
     * 币种类型
     */
    private String currency_type;

    /**
     * 开始时间
     */
    private LocalDateTime start_time;

    /**
     * 结束时间
     */
    private LocalDateTime end_time;

    /**
     * 管理员名称
     */
    private String admin_username;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 备注
     */
    private String remark;

    /**
     * 押注结果走向
     */
    private BetResultEnum result;

    public static DirectionalConfigVO trans(DirectionalConfig dc, LocalDateTime now){
        DirectionalConfigStatus status = null;
        if(now.isBefore(dc.getStart_time())){
            status = DirectionalConfigStatus.ineffective;
        } else if(now.isAfter(dc.getEnd_time())){
            status = DirectionalConfigStatus.expired;
        } else {
            status = DirectionalConfigStatus.effective;
        }
        return DirectionalConfigVO.builder()
                .id(dc.getId())
                .status(status)
                .currency_type(dc.getCurrency_type())
                .result(dc.getResult())
                .start_time(dc.getStart_time())
                .end_time(dc.getEnd_time())
                .admin_username(dc.getAdmin_username())
                .create_time(dc.getCreate_time())
                .remark(dc.getRemark()).build();
    }
}
