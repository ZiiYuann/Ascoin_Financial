package com.tianli.operate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Objects;

/**
 * @author linyifan
 *  2/24/21 2:58 PM
 */
@Mapper
public interface OperateMapper extends BaseMapper<Operate> {

    //返回给客户端
    @SelectProvider(type = GenerateSQL.class, method = "getClientPageList")
    List<OperateVO> getClientPageList(@Param("location") Integer location,
                                      @Param("type") Integer type,
                                      @Param("offset") Integer offset,
                                      @Param("size") Integer size,
                                      @Param("currentTime") long currentTime);

    //返回给管理端
    @SelectProvider(type = GenerateSQL.class, method = "pageList")
    List<OperateVO> pageList(@Param("location") Integer location,
                             @Param("type") Integer type,
                             @Param("offset") Integer offset,
                             @Param("size") Integer size);

    @SelectProvider(type = GenerateSQL.class, method = "operateCount")
    Integer countOperates(@Param("location") Integer location,
                          @Param("type") Integer type);

    @Select("select ifNull(max(sort),0) from operate")
    Integer getMaxSort();

    class GenerateSQL{

        public String getClientPageList(@Param("location") Integer location,
                                        @Param("type")Integer type,
                                        @Param("offset")Integer offset,
                                        @Param("size")Integer size,
                                        @Param("currentTime")long currentTime){
            return getClientList(location,type).toString();
        }

        private SQL getClientList(Integer location,Integer type){

            String select = "o.id as operateId, o.sort as sort, o.type,o.picture,o.online,o.start_time as startTime," +
                    "o.end_time as endTime,o.create_time as createTime,o.validity,o.url,t.page_location,t.id as id";
            SQL sql = new SQL().SELECT(select)
                    .FROM("`operate` AS o")
                    .RIGHT_OUTER_JOIN("`operate_type` AS t ON o.id = t.operate_id")
                    .WHERE(" o.online = 1");
            if (Objects.nonNull(location)){
                sql.WHERE("t.`page_location` = #{location}");
            }
            if (Objects.nonNull(type)){
                sql.WHERE("o.`type` = #{type}");
            }
            sql.ORDER_BY("o.`sort` ASC,o.`create_time` DESC");
            return sql;
        }

        public String pageList(@Param("location") Integer location,
                               @Param("type")Integer type,
                               @Param("offset")Integer offset,
                               @Param("size")Integer size){
            return getList(location,type,offset,size).toString();
        }

        private SQL getList(Integer location,Integer type,Integer offset,Integer size){

            String select = "o.id as operateId,o.sort,o.type,o.picture,o.online,o.start_time as startTime," +
                    "o.end_time as endTime,o.create_time as createTime,o.validity,o.url,t.page_location,t.id as id";
            SQL sql = new SQL().SELECT(select)
                    .FROM("`operate` AS o")
                    .RIGHT_OUTER_JOIN("`operate_type` AS t ON o.id = t.operate_id");
            if (Objects.nonNull(location)){
                sql.WHERE("t.`page_location` = #{location}");
            }
            if (Objects.nonNull(type)){
                sql.WHERE("o.`type` = #{type}");
            }
            sql.ORDER_BY("o.`sort` ASC").LIMIT("#{offset},#{size}");
            return sql;
        }

        public String operateCount(@Param("location") Integer location,
                                   @Param("type")Integer type){
            return getCount(location,type).toString();
        }

        private SQL getCount(Integer location,Integer type){

            String select = "count(t.id)";
            SQL sql = new SQL().SELECT(select)
                    .FROM("`operate` AS o")
                    .RIGHT_OUTER_JOIN("`operate_type` AS t ON o.id = t.operate_id");
            if (Objects.nonNull(location)){
                sql.WHERE("t.`page_location` = #{location}");
            }
            if (Objects.nonNull(type)){
                sql.WHERE("o.`type` = #{type}");
            };
            return sql.ORDER_BY("`t`.id");
        }


    }

}
