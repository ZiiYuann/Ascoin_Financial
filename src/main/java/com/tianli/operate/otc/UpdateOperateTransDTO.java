package com.tianli.operate.otc;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author linyifan
 *  2/24/21 3:22 PM
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class UpdateOperateTransDTO {

    /**
     * id
     */
    private Long id;

    /**
     * operate表ID
     */
    @NotNull(message = "id不能为空")
    private Long operateId;

    /**
     *页面标识 页面多选的
     */
    private Integer location;
    private List<Integer> locations;

    /**
     *图片
     */
    @NotNull(message = "图片不能为空")
    private String picture;

    /**
     * 排序
     */
    @Min(value = 0, message = "排序权重大于0")
    private Integer sort;

    /**
     *是否上线
     */
    @NotNull(message = "是否上线不能为空")
    private Boolean online;

    /**
     *跳转链接
     */
    private String url;

    /**
     *有效期起始时间
     */
    private LocalDateTime start_time;

    /**
     *有效期结束时间
     */
    private LocalDateTime end_time;

    /**
     * 是否长期有效validity
     */
    private Boolean validity;

    /**
     * 图片类型
     */
    private Integer type;

    public static UpdateOperateTransDTO trans(UpdateOperateDTO dto){
        return UpdateOperateTransDTO.builder()
                .id(dto.getId())
                .operateId(dto.getOperateId())
                .location(dto.getLocation())
                .locations(dto.getLocations())
                .picture(dto.getPicture())
                .sort(dto.getSort())
                .online(dto.getOnline())
                .url(dto.getUrl())
                .start_time(dto.getStartTime())
                .end_time(dto.getEndTime())
                .validity(dto.getValidity())
                .type(dto.getType()).build();
    }



}
