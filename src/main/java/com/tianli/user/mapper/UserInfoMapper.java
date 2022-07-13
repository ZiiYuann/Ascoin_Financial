package com.tianli.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.user.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {


    @Select("select * from user_info where sign_address = #{signAddress} and sign_chain =#{signChain}")
    UserInfo getBySignInfo(@Param("signAddress") String signAddress, @Param("signChain") String signChain);

}
