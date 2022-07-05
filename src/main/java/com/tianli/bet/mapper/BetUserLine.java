package com.tianli.bet.mapper;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 押注表
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class BetUserLine {

    private static final long serialVersionUID = 3301837360994547112L;

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private int id;
    /**
     * 用户id
     */
    private Long uid;

    /**
     * symbol
     */
    private String symbol;

    /**
     * 用户username
     */
    private String line_json;

    /**
     * 版本号
     */
    private int version;
}
