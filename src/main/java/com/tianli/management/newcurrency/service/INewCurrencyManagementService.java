package com.tianli.management.newcurrency.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.exception.Result;
import com.tianli.management.newcurrency.entity.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * <p>
 * 新币管理端 服务类
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
public interface INewCurrencyManagementService extends IService<NewCurrencyManagement> {

    Result page(NewCurrencyManagementDTO newCurrencyManagement, Long page, Long size);

    Result insert(NewCurrencyManagement list);

    Result updateByEntity(@RequestBody NewCurrencyManagement list);

    Result deleteById(Long id);

    Result getListById(Long id);

    List<NewCurrencyManagement> queryOnlineToken();

}
