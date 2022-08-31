package com.tianli.management.bo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class WalletAgentBO {
    /**
     * ID
     */
    private Long id;

    /**
     * 代理人ID
     */
    @NotNull(message = "代理人ID不能为空")
    private Long uid;

    /**
     * 代理人名称
     */
    @NotNull(message = "代理人名称不能为空")
    private String agentName;

    /**
     * 备注
     */
    @Length(max = 300,message = "备注字符长度不能超过300")
    private String remark;


    /**
     * 子产品列表
     */
    @NotNull(message = "子产品不能为空")
    @Size(min = 1,message = "子产品不能为空")
    private List<Product> products;

    @Data
    public static class Product{

        //产品ID
        @NotNull(message = "产品ID不能为空")
        private Long productId;

        //推荐码
        @NotNull(message = "推荐码不能为空")
        private String referralCode;

    }
}
