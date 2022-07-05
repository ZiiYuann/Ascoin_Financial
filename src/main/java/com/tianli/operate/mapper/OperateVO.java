package com.tianli.operate.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author linyifan
 *  2/25/21 11:33 AM
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class OperateVO {

    /**
     * operateId
     */
    private Long operateId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     *页面
     */
    private Integer type;

    /**
     *图片
     */
    private String picture;

    /**
     *是否上线
     */
    private Boolean online;

    /**
     *有效期起始时间
     */
    private LocalDateTime startTime;

    /**
     *有效期结束时间
     */
    private LocalDateTime endTime;

    /**
     *创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否长期有效
     */
    private Boolean validity;

    /**
     *跳转链接
     */
    private String url;

    /**
     * 页面标识
     */
    private Integer location;

    /**
     * 页面标识id
     */
    private List<Integer> locations;

    /**
     * 页面标识
     */
    private String locationsDes;

    /**
     * id
     */
    private Long id;

    public static OperateVO trans(Operate operate){
        return OperateVO.builder()
                .operateId(operate.getId())
                .sort(operate.getSort())
                .type(operate.getType())
                .picture(operate.getPicture())
                .online(operate.getOnline())
                .startTime(operate.getStart_time())
                .endTime(operate.getEnd_time())
                .createTime(operate.getCreate_time())
                .validity(operate.getValidity())
                .url(operate.getUrl()).build();
    }

}
