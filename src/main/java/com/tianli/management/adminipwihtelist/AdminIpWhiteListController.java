package com.tianli.management.adminipwihtelist;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.admin.AdminAndRoles;
import com.tianli.admin.AdminService;
import com.tianli.admin.mapper.Admin;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.Result;
import com.tianli.management.adminipwihtelist.dto.AdminIpWhiteListDTO;
import com.tianli.management.adminipwihtelist.mapper.AdminIpWhiteList;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.IpAddressTool;
import com.tianli.tool.MapTool;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/management/admin/white/ip")
public class AdminIpWhiteListController {

    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.ip白名单)
    public Result list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                       String ip
    ) {
        List<AdminIpWhiteList> list = adminIpWhiteListService.page(new Page<>(page, size),
                new LambdaQueryWrapper<AdminIpWhiteList>().eq(AdminIpWhiteList::getIs_deleted, 0)
                .eq(Objects.nonNull(ip), AdminIpWhiteList::getIp, ip)
                .orderByDesc(AdminIpWhiteList::getCreate_time)
        ).getRecords();
        long count = adminIpWhiteListService.count(
                new LambdaQueryWrapper<AdminIpWhiteList>().eq(AdminIpWhiteList::getIs_deleted, 0)
                        .eq(Objects.nonNull(ip), AdminIpWhiteList::getIp, ip)
        );
        return Result.instance().setList(list, count);
    }

    @PostMapping("/save")
    @AdminPrivilege(and = Privilege.ip白名单)
    public Result save(@RequestBody AdminIpWhiteListDTO adminIpWhiteListDTO) {
        AdminAndRoles myAndRole = adminService.my();
        Admin admin = adminService.getByUsername(myAndRole.getUsername());
        AdminIpWhiteList adminIpWhiteList = AdminIpWhiteList.builder()
                .admin_id(admin.getId()).admin_nickname(admin.getNickname()).admin_username(admin.getUsername())
                .ip(adminIpWhiteListDTO.getIp()).note(adminIpWhiteListDTO.getNote())
                .is_deleted(0).id(CommonFunction.generalId()).create_time(requestInitService.now())
                .ip_address(ipAddressTool.getAddress(adminIpWhiteListDTO.getIp())).build();
        adminIpWhiteListService.save(adminIpWhiteList);
        return Result.instance();
    }

    @GetMapping("/delete")
    @AdminPrivilege(and = Privilege.ip白名单)
    public Result delete(Long id) {
        AdminIpWhiteList adminIpWhiteList = adminIpWhiteListService.getById(id);
        if(adminIpWhiteList == null) return Result.instance();
        adminIpWhiteList.setIs_deleted(1);
        adminIpWhiteListService.updateById(adminIpWhiteList);
        return Result.instance();
    }


    @GetMapping("/address")
    @AdminPrivilege(and = Privilege.ip白名单)
    public Result ipAddress(String ip) {
        String ip_address = ipAddressTool.getAddress(ip);
        return Result.instance().setData(MapTool.Map().put("ip_address", ip_address));
    }

    @Resource
    private AdminIpWhiteListService adminIpWhiteListService;
    @Resource
    private IpAddressTool ipAddressTool;
    @Resource
    private AdminService adminService;
    @Resource
    private RequestInitService requestInitService;
}
