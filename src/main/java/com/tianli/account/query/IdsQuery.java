package com.tianli.account.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-18
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdsQuery {

    private List<Long> ids;

    private Long id;

    private Long uid;

    private Long chatId;

}
