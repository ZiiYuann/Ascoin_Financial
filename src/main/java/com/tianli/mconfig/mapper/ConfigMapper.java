package com.tianli.mconfig.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.user.mapper.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @Author wangqiyun
 * @Date 2019-11-13 17:33
 */

@Mapper
public interface ConfigMapper extends BaseMapper<Config> {

    @Insert("REPLACE INTO `config`(`name`, `value`) VALUES (#{name},#{value})")
    int replace(Config config);
}
