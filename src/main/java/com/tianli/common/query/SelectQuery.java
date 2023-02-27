package com.tianli.common.query;

import com.tianli.common.annotation.QueryWrapperGenerator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-20
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectQuery {

    @QueryWrapperGenerator(field = "uid")
    private Long uid;

    @QueryWrapperGenerator(field = "coin")
    private String coin;

}
