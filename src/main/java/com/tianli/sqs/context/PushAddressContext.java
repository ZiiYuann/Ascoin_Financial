package com.tianli.sqs.context;

import com.tianli.chain.entity.Coin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-28
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushAddressContext {

    private List<String> addresses;

    private Coin coin;
}
