package com.tianli.management.salesman.vo;

import com.tianli.management.salesman.entity.Salesman;
import lombok.Builder;
import lombok.Data;

/**
 * @author lzy
 * @date 2022/4/6 5:57 下午
 */
@Builder
@Data
public class SalesmanLeaderListVo {

    private Long leader_id;

    private String username;

    private String kf_url;


    public static SalesmanLeaderListVo collect(Salesman salesman) {
        return SalesmanLeaderListVo.builder()
                .leader_id(salesman.getId())
                .username(salesman.getAdmin_username())
                .kf_url(salesman.getKf_url()).build();
    }
}
