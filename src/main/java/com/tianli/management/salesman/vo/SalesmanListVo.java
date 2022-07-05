package com.tianli.management.salesman.vo;

import cn.hutool.core.collection.CollUtil;
import com.tianli.management.salesman.entity.Salesman;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lzy
 * @date 2022/4/6 6:20 下午
 */
@Builder
@Data
public class SalesmanListVo {
    private Long id;

    private String name;

    private String remark;

    private String kf_url;

    private List<SalesmanListVo> salesmanListVoList;


    public static SalesmanListVo collect(Salesman salesman, List<Salesman> teamMemberSalesmanList) {
        SalesmanListVo salesmanListVo = collect(salesman);
        if (CollUtil.isNotEmpty(teamMemberSalesmanList)) {
            List<SalesmanListVo> salesmanListVos = teamMemberSalesmanList.stream().map(SalesmanListVo::collect).collect(Collectors.toList());
            salesmanListVo.setSalesmanListVoList(salesmanListVos);
        }
        return salesmanListVo;
    }


    public static SalesmanListVo collect(Salesman salesman) {
        return SalesmanListVo.builder()
                .id(salesman.getId())
                .name(salesman.getAdmin_username())
                .remark(salesman.getRemark())
                .kf_url(salesman.getKf_url())
                .build();
    }
}
