package com.tianli.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-14
 **/
@Data
public class PageQuery<T> {


    private int page;

    private int pageSize;

    public Page<T> page(){
        return new Page<T>(getPage(),getPageSize());
    }
}
