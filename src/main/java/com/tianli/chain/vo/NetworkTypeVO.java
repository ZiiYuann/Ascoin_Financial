package com.tianli.chain.vo;

import com.tianli.chain.enums.ChainType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-12
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetworkTypeVO {

    private Set<String> networkTypes;

    private ChainType chainType;

}
