package com.tianli.tool;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wangqiyun on 2018/7/20.
 */
public class JsonTypeHandler extends BaseTypeHandler<Map<String, Object>> {
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, Map<String, Object> map, JdbcType jdbcType) throws SQLException {
        preparedStatement.setString(i, new Gson().toJson(map));
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String str = resultSet.getString(s);
        return str != null ? new Gson().fromJson(str, new TypeToken<LinkedHashMap<String, Object>>() {
        }.getType()) : null;
    }

    @Override
    public Map<String, Object> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String str = resultSet.getString(i);
        return str != null ? new Gson().fromJson(str, new TypeToken<LinkedHashMap<String, Object>>() {
        }.getType()) : null;
    }

    @Override
    public Map<String, Object> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String str = callableStatement.getString(i);
        return str != null ? new Gson().fromJson(str, new TypeToken<LinkedHashMap<String, Object>>() {
        }.getType()) : null;
    }
}
