package com.tianli.exchange.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lzy
 * @date 2022/6/27 11:08
 */
@Data
@Builder
public class DepthVo implements Serializable {
    private List<List<String>> bids;

    private List<List<String>> asks;

}
