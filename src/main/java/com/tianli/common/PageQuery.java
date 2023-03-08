package com.tianli.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-14
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageQuery<T> {

    private int page = 1;

    private int pageSize = 10;

    public int getOffset(){
        return (page - 1) * pageSize;
    }

    public Page<T> page(){
        return new Page<T>(getPage(),getPageSize());
    }
}
