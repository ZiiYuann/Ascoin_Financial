package com.tianli.exchange.dao;

import com.tianli.exchange.entity.KLinesInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author lzy
 * @since 2022-06-09
 */
@Mapper
public interface KLinesInfoMapper extends BaseMapper<KLinesInfo> {

    void addBatch(@Param("kLinesInfos") List<KLinesInfo> kLinesInfos);
}
