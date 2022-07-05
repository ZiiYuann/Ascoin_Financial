package com.tianli.admin.adminiplog;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.admin.adminiplog.mapper.AdminIpLog;
import com.tianli.admin.adminiplog.mapper.AdminIpLogMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 *     管理员登录 ip记录表
 * </P>
 *
 * @author linyifan
 * @since 5/12/21 11:36 AM
 */

@Service
public class AdminIpLogService extends ServiceImpl<AdminIpLogMapper, AdminIpLog> {
}
