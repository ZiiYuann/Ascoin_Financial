package com.tianli.chain.entity;

import com.tianli.chain.enums.ChainType;
import com.tianli.charge.enums.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-02
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChainCallbackLog {

    @Id
    private Long id;

    private ChargeType type;

    private ChainType chain;

    private String log;

    private String status;

    private String msg;

    private LocalDateTime createTime;

}
