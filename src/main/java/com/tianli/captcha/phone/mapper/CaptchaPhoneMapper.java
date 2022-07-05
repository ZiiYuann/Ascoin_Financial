package com.tianli.captcha.phone.mapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

/**
 * @Author wangqiyun
 * @Date 2018/12/5 5:43 PM
 */
@Mapper
public interface CaptchaPhoneMapper {
    @Insert("INSERT INTO `captcha_phone`(`id`, `create_time`, `phone`, `type`, `code`, `region`) VALUES " +
            "(#{id},#{create_time},#{phone},#{type},#{code},#{region})")
    long insert(CaptchaPhone captchaPhone);



    @SelectProvider(type = GenerateSQL.class, method = "selectCaptchaPhone")
    List<CaptchaPhone> selectCaptchaPhone(@Param("phone") String phone,
                                          @Param("page") Integer page,
                                          @Param("size") Integer size);

    @SelectProvider(type = GenerateSQL.class, method = "selectCount")
    int selectCount(String phone);

    @Select("SELECT `region` FROM `captcha_phone` WHERE `phone` = #{phone} order by `ID` desc limit 1")
    String selectRegion(@Param("phone") String phone);

    @Select("select * from sms_channel")
    List<SmsChannel> selectAll();

    @Select("select count(*) from  sms_channel where `number` = #{num}")
    Integer countByNum(@Param("num") String num);

    class GenerateSQL{
        public String selectCaptchaPhone(String phone, Integer page, Integer size){
            String sqlString = mySql(" id, create_time, phone, code, type, region ", phone).ORDER_BY(" `id` DESC ").toString();
            return sqlString + " limit " + Math.max((page - 1) * size, 0) + " , " + size;
        }

        public String selectCount(String phone){
            return mySql(" count(*) ", phone).toString();
        }

        private SQL mySql(String select, String phone){
            SQL sql = new SQL().SELECT(select).FROM(" `captcha_phone` ");
            if (StringUtils.isNotBlank(phone)){
                sql.WHERE(" `phone` like CONCAT('%',#{phone},'%')");
            }
            return sql;
        }
    }
}
