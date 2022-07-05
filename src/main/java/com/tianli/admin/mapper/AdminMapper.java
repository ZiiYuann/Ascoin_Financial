package com.tianli.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.admin.AdminPageVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

/**
 * <p>
 * 管理员 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    @SelectProvider(type = GenerateSQL.class, method = "pageAdmin")
    List<AdminPageVO> pageAdmin(@Param("role_name") String role_name,
                                @Param("username") String username,
                                @Param("size") Integer size,
                                @Param("offset") Integer offset);

    @SelectProvider(type = GenerateSQL.class, method = "countAdmin")
    int countAdmin(@Param("role_name") String role_name,
                   @Param("username") String username);

    @Select("select rp.permission from admin ad left join admin_role ar on ad.`id` = ar.uid " +
            "left join role_permission rp on ar.role_id = rp.role_id WHERE ad.`id` = #{id}")
    List<String> getPrivilege(@Param(("id")) Long id);

    class GenerateSQL{

        private SQL countSQL(String role_name, String username){
            SQL sql = new SQL().SELECT("count(1)")
                    .FROM("`admin` AS ad")
                    .LEFT_OUTER_JOIN("`admin_role` AS ar ON ad.id = ar.uid")
                    .LEFT_OUTER_JOIN("`admin_totp` AS totp ON ad.id = totp.id")
                    .LEFT_OUTER_JOIN("`role` AS ro ON ar.role_id = ro.id");
            if(StringUtils.isNotBlank(role_name)&&!"所有".equals(role_name)){
                sql.WHERE("`name` = #{role_name}");
            }
            if(StringUtils.isNotBlank(username)){
                sql.WHERE("`username` like CONCAT('%',#{username},'%')");
            }
            return sql;
        }

        public String countAdmin(@Param("role_name") String role_name,
                                 @Param("username") String username){
            return countSQL(role_name,username).toString();
        }

        private SQL mySQL(String role_name, String username, Integer size, Integer offset){
            String select = "ad.id,ad.nickname,ad.username,ad.`status`,ad.phone,ad.note,ro.`name` AS role_name,ad.last_ip, totp.id as google_secret_id";
            SQL sql = new SQL().SELECT(select)
                    .FROM("`admin` AS ad")
                    .LEFT_OUTER_JOIN("`admin_role` AS ar ON ad.id = ar.uid")
                    .LEFT_OUTER_JOIN("`admin_totp` AS totp ON ad.id = totp.id")
                    .LEFT_OUTER_JOIN("`role` AS ro ON ar.role_id = ro.id");
            if(StringUtils.isNotBlank(role_name)){
                sql.WHERE("ro.`name` = #{role_name}");
            }
            if(StringUtils.isNotBlank(username)){
                sql.WHERE("ad.`username` like CONCAT('%',#{username},'%')");
            }
            sql.ORDER_BY("ad.`create_time` DESC").LIMIT("#{offset},#{size}");
            return sql;
        }

        public String pageAdmin(@Param("role_name") String role_name,
                                @Param("username") String username,
                                @Param("size") Integer size,
                                @Param("offset") Integer offset){
            return mySQL(role_name,username,size, offset).toString();
        }
    }
}
