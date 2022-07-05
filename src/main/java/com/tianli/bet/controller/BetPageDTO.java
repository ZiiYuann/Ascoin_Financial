package com.tianli.bet.controller;

import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.bet.mapper.BetTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
public class BetPageDTO implements Serializable {

    /**
     * 押注类型
     */
    private BetTypeEnum betType;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 用户输赢结果
     */
    private BetResultEnum result;

    /**
     * 上一页的最下面的记录id
     *
     * 第一页时不用传/小于0
     */
    private long fromId = -1L;

    /**
     * 每页大小
     */
    private int size = 20;

    /**
     * 币种类型
     */
    private String bet_symbol;
}
