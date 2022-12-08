package com.tianli.management.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-08
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UidsQuery {

    private List<Long> uids;
}
