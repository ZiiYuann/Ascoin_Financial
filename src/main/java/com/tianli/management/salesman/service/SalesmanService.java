package com.tianli.management.salesman.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.admin.AdminAndRoles;
import com.tianli.admin.AdminService;
import com.tianli.admin.mapper.Admin;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.CommonFunction;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.admin.AdminUpdatePwdDTO;
import com.tianli.management.admin.UpdateAdminDTO;
import com.tianli.management.salesman.dao.SalesmanMapper;
import com.tianli.management.salesman.dto.CustomerAssignmentDto;
import com.tianli.management.salesman.dto.SalesmanEditDto;
import com.tianli.management.salesman.entity.Salesman;
import com.tianli.management.salesman.entity.SalesmanUser;
import com.tianli.management.salesman.enums.SalesmanEnum;
import com.tianli.management.salesman.vo.SalesmanInfoListVo;
import com.tianli.management.salesman.vo.SalesmanLeaderListVo;
import com.tianli.management.salesman.vo.SalesmanListVo;
import com.tianli.management.spot.entity.SGCharge;
import com.tianli.management.spot.service.SGChargeService;
import com.tianli.role.RoleService;
import com.tianli.role.mapper.Role;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lzy
 * @date 2022/4/6 4:12 下午
 */
@Service
public class SalesmanService extends ServiceImpl<SalesmanMapper, Salesman> {

    @Resource
    AdminService adminService;

    @Resource
    RoleService roleService;

    @Resource
    UserService userService;

    @Resource
    SalesmanUserService salesmanUserService;

    @Resource
    CurrencyLogService currencyLogService;

    @Resource
    ChargeService chargeService;

    @Resource
    SGChargeService sgChargeService;


    @Transactional(rollbackFor = Exception.class)
    public void edit(SalesmanEditDto salesmanEditDto) {
        Long id = salesmanEditDto.getId();
        Salesman salesman;
        AdminAndRoles my = adminService.my();
        if (ObjectUtil.isNotNull(id)) {
            salesman = getUpdateSalesman(salesmanEditDto, id, my);
        } else {
            salesman = getInsertSalesman(salesmanEditDto, my);
        }
        if (ObjectUtil.isNotNull(salesmanEditDto.getLeader_id()) && salesman.getId().equals(salesmanEditDto.getLeader_id())) {
            throw ErrorCodeEnum.WRONG_SETTINGS.generalException();
        }
        //设置客服参数
        setKeFuUrl(salesmanEditDto, salesman);
        this.saveOrUpdate(salesman);
    }

    private void setKeFuUrl(SalesmanEditDto salesmanEditDto, Salesman salesman) {
        String keFuUrl = salesmanEditDto.getKf_url();
        if (ObjectUtil.isNull(salesman.getP_id())) {
            this.update(Wrappers.lambdaUpdate(Salesman.class)
                    .set(Salesman::getKf_url, keFuUrl)
                    .set(Salesman::getUpdate_time, LocalDateTime.now())
                    .eq(Salesman::getP_id, salesman.getId()));
        } else {
            Salesman pSalesman = this.getOne(Wrappers.lambdaQuery(Salesman.class).eq(Salesman::getId, salesman.getP_id()));
            if (ObjectUtil.isNotNull(pSalesman) && StrUtil.isNotBlank(pSalesman.getKf_url())) {
                keFuUrl = pSalesman.getKf_url();
            }
        }
        salesman.setKf_url(keFuUrl);
    }


    private Salesman getInsertSalesman(SalesmanEditDto salesmanEditDto, AdminAndRoles my) {
        String roleName = my.getRole().getName();
        Salesman currentSalesman = this.getOne(Wrappers.lambdaQuery(Salesman.class).eq(Salesman::getAdmin_id, my.getId()));
        if (StrUtil.equals(roleName, SalesmanEnum.业务员.name())) {
            throw ErrorCodeEnum.ACCESS_DENY.generalException();
        } else if (StrUtil.equals(roleName, SalesmanEnum.业务主管.name())) {
            salesmanEditDto.setLeader_id(currentSalesman.getId());
        }
        Salesman salesman;
        SalesmanEnum salesmanEnum = ObjectUtil.isNull(salesmanEditDto.getLeader_id()) ? SalesmanEnum.业务主管 : SalesmanEnum.业务员;
        Long adminId = adminService.createAdmin(salesmanEditDto.getCreateAdminDTO(salesmanEnum));
        salesman = Salesman.builder()
                .id(CommonFunction.generalId())
                .admin_id(adminId)
                //.creator("测试")
                //.creator_id(111L)
                .creator(my.getUsername())
                .creator_id(my.getId())
                .admin_username(salesmanEditDto.getUsername())
                .remark(salesmanEditDto.getRemark())
                .create_time(LocalDateTime.now())
                .p_id(salesmanEditDto.getLeader_id())
                .is_deleted(Boolean.FALSE)
                .build();
        return salesman;
    }

    private Salesman getUpdateSalesman(SalesmanEditDto salesmanEditDto, Long id, AdminAndRoles my) {
        Salesman salesman;
        salesman = this.getById(id);
        if (ObjectUtil.isNull(salesman)) {
            throw ErrorCodeEnum.SALESMAN_NOT_FOUND.generalException();
        }
        String roleName = my.getRole().getName();
        setSalesmanRole(salesmanEditDto, id, my, salesman, roleName);
        if (StrUtil.isNotBlank(salesmanEditDto.getPassword())) {
            adminService.updatePwd(salesman.getAdmin_id(), AdminUpdatePwdDTO.builder().password(salesmanEditDto.getPassword()).build());
        }
        if (!StrUtil.equals(my.getUsername(), salesmanEditDto.getUsername())) {
            Admin admin = adminService.getById(salesman.getAdmin_id());
            admin.setUsername(salesmanEditDto.getUsername());
            Role role = roleService.getByAid(salesman.getAdmin_id());
            this.updateAdmin(admin, Objects.requireNonNull(SalesmanEnum.getSalesmanEnum(role.getName())));
            salesman.setAdmin_username(salesmanEditDto.getUsername());
        }
        salesman.setUpdate_time(LocalDateTime.now()).setRemark(salesmanEditDto.getRemark());
        return salesman;
    }

    private void setSalesmanRole(SalesmanEditDto salesmanEditDto, Long id, AdminAndRoles my, Salesman salesman, String roleName) {
        if (StrUtil.equals(SalesmanEnum.业务员.name(), roleName) || StrUtil.equals(SalesmanEnum.业务主管.name(), roleName)) {
            Salesman currentSalesman = this.getOne(Wrappers.lambdaQuery(Salesman.class).eq(Salesman::getAdmin_id, my.getId()));
            if (ObjectUtil.notEqual(currentSalesman.getId(), salesman.getId()) && ObjectUtil.notEqual(currentSalesman.getId(), salesman.getP_id())) {
                throw ErrorCodeEnum.ACCESS_DENY.generalException();
            }
        } else {
            //管理员修改的
            Role role = roleService.getByAid(salesman.getAdmin_id());
            Admin admin = adminService.getById(salesman.getAdmin_id());
            //将业务主管降级分配到其他组
            if (StrUtil.equals(SalesmanEnum.业务主管.name(), role.getName()) && ObjectUtil.isNotNull(salesmanEditDto.getLeader_id())) {
                if (ObjectUtil.equal(salesmanEditDto.getLeader_id(), id)) {
                    throw ErrorCodeEnum.WRONG_SETTINGS.generalException();
                }
                //查询下面是否有组员
                int count = this.count(Wrappers.lambdaQuery(Salesman.class).eq(Salesman::getP_id, id));
                if (count > 0) {
                    throw ErrorCodeEnum.EXIST_LOW_SALESMAN.generalException();
                }
                //将角色改为业务员
                updateAdmin(admin, SalesmanEnum.业务员);
                salesman.setP_id(salesmanEditDto.getLeader_id());
            } else if (StrUtil.equals(SalesmanEnum.业务员.name(), role.getName()) && ObjectUtil.isNotNull(salesmanEditDto.getLeader_id())) {
                salesman.setP_id(salesmanEditDto.getLeader_id());
            } else if (StrUtil.equals(SalesmanEnum.业务员.name(), role.getName()) && ObjectUtil.isNull(salesmanEditDto.getLeader_id())) {
                //将角色改为业务主管
                updateAdmin(admin, SalesmanEnum.业务主管);
                salesman.setP_id(null);
            }
        }
    }

    private void updateAdmin(Admin admin, SalesmanEnum salesmanEnum) {
        UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO();
        updateAdminDTO.setId(admin.getId());
        updateAdminDTO.setUsername(admin.getUsername());
        updateAdminDTO.setNickname(admin.getNickname());
        updateAdminDTO.setPhone(admin.getPhone());
        updateAdminDTO.setNote(admin.getNote());
        updateAdminDTO.setRole_name(salesmanEnum.name());
        adminService.updateAdmin(updateAdminDTO);
    }

    @Transactional
    public void deleteSalesman(Long id) {
        Salesman salesman = this.getById(id);
        if (ObjectUtil.isNull(salesman)) {
            throw ErrorCodeEnum.SALESMAN_NOT_FOUND.generalException();
        }
        if (ObjectUtil.isNull(salesman.getP_id())) {
            //查询下面是否有组员
            int count = this.count(Wrappers.lambdaQuery(Salesman.class).eq(Salesman::getP_id, id));
            if (count > 0) {
                throw ErrorCodeEnum.EXIST_LOW_SALESMAN.generalException();
            }
        }
        adminService.deleteAdmin(salesman.getAdmin_id());
        salesmanUserService.remove(Wrappers.lambdaQuery(SalesmanUser.class).eq(SalesmanUser::getSalesman_id, salesman.getId()));
        this.removeById(salesman);
    }

    public List<SalesmanLeaderListVo> leaderList() {
        AdminAndRoles my = adminService.my();
        Salesman currentSalesman = this.getOne(Wrappers.lambdaQuery(Salesman.class).eq(Salesman::getAdmin_id, my.getId()));
        LambdaQueryWrapper<Salesman> wrapper = Wrappers.lambdaQuery(Salesman.class)
                .isNull(Salesman::getP_id)
                .orderByDesc(Salesman::getCreate_time);
        if (StrUtil.equals(SalesmanEnum.业务员.name(), my.getRole().getName())) {
            wrapper.eq(Salesman::getP_id, currentSalesman.getP_id());
        } else if (StrUtil.equals(SalesmanEnum.业务主管.name(), my.getRole().getName())) {
            wrapper.eq(Salesman::getId, currentSalesman.getId());
        }
        List<Salesman> salesmanList = this.list(wrapper);
        if (CollUtil.isEmpty(salesmanList)) {
            return null;
        }
        return salesmanList.stream().map(SalesmanLeaderListVo::collect).collect(Collectors.toList());
    }

    public IPage<SalesmanListVo> salesmanList(Integer page, Integer size, String username) {
        LambdaQueryWrapper<Salesman> wrapper = Wrappers.lambdaQuery(Salesman.class);
        AdminAndRoles my = adminService.my();
        if (SalesmanEnum.业务员.name().equals(my.getRole().getName()) || StrUtil.equals(SalesmanEnum.业务主管.name(), my.getRole().getName())) {
            wrapper.eq(Salesman::getAdmin_id, my.getId());
        }
        if (StrUtil.isNotBlank(username)) {
            wrapper.like(Salesman::getAdmin_username, username);
        } else {
            wrapper.isNull(Salesman::getP_id);
        }
        wrapper.orderByDesc(Salesman::getCreate_time);
        Page<Salesman> salesmanPage = this.page(new Page<>(page, size), wrapper);
        List<Salesman> salesmanList = salesmanPage.getRecords();
        if (CollUtil.isEmpty(salesmanList)) {
            return new Page<>(page, size);
        }
        List<Long> salesmenIds = salesmanList.stream().filter(salesman -> ObjectUtil.isNull(salesman.getP_id())).map(Salesman::getId).collect(Collectors.toList());
        Map<Long, List<Salesman>> teamMemberMap = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(salesmenIds)) {
            List<Salesman> teamMemberSalesmen = this.list(Wrappers.lambdaQuery(Salesman.class)
                    .in(Salesman::getP_id, salesmenIds)
                    .orderByDesc(Salesman::getCreate_time));
            if (CollUtil.isNotEmpty(teamMemberSalesmen)) {
                teamMemberMap = teamMemberSalesmen.stream().collect(Collectors.groupingBy(Salesman::getP_id));
            }
        }
        final Map<Long, List<Salesman>> finalTeamMemberMap = teamMemberMap;
        List<SalesmanListVo> result = salesmanList.stream().map(salesman -> SalesmanListVo.collect(salesman, finalTeamMemberMap.get(salesman.getId()))).collect(Collectors.toList());
        return new Page<SalesmanListVo>()
                .setCurrent(salesmanPage.getCurrent())
                .setSize(salesmanPage.getSize())
                .setRecords(result)
                .setTotal(salesmanPage.getTotal())
                .setPages(salesmanPage.getPages());
    }

    @Transactional(rollbackFor = Exception.class)
    public void customerAssignment(CustomerAssignmentDto customerAssignmentDto) {
        Salesman salesman = this.getById(customerAssignmentDto.getSalesmanId());
        if (ObjectUtil.isNull(salesman)) {
            throw ErrorCodeEnum.SALESMAN_NOT_FOUND.generalException();
        }
        salesmanUserService.removeByUserIds(customerAssignmentDto.getUserIds());
        AdminAndRoles my = adminService.my();
        List<SalesmanUser> salesmanUsers = customerAssignmentDto.getUserIds().stream()
                .map(useId -> SalesmanUser.collectCustomerAssignmentDto(useId, salesman, my))
                .collect(Collectors.toList());
        salesmanUserService.saveBatch(salesmanUsers);
    }

    public IPage<SalesmanInfoListVo> infoList(Integer page, Integer size, Long id) {
        AdminAndRoles my = adminService.my();
        LambdaQueryWrapper<Salesman> wrapper = Wrappers.lambdaQuery(Salesman.class);
        setPurview(id, my, wrapper);
        wrapper.orderByDesc(Salesman::getCreate_time);
        Page<Salesman> salesmanPage = this.page(new Page<>(page, size), wrapper);
        List<Salesman> salesmanList = salesmanPage.getRecords();
        if (CollUtil.isEmpty(salesmanList)) {
            return new Page<SalesmanInfoListVo>(page, size).setRecords(new ArrayList<>());
        }
        List<Long> salesmanIds = salesmanList.stream().map(Salesman::getId).collect(Collectors.toList());
        List<SalesmanUser> salesmanUsers = salesmanUserService.list(Wrappers.lambdaQuery(SalesmanUser.class).in(SalesmanUser::getSalesman_id, salesmanIds));
        List<SalesmanInfoListVo> infoListVos;
        if (CollUtil.isNotEmpty(salesmanUsers)) {
            Map<Long, List<SalesmanUser>> salesmanUserMap = salesmanUsers.stream().collect(Collectors.groupingBy(SalesmanUser::getSalesman_id));
            List<Long> userIds = salesmanUsers.stream().map(SalesmanUser::getUser_id).collect(Collectors.toList());
            List<User> users = userService.getByIds(userIds);
            Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity()));
            List<String> des = ListUtil.of(ChargeType.recharge.name(), ChargeType.withdraw.name());
            Map<Long, List<Charge>> chargeMap = getChargeMap(userIds, des);
            Map<Long, List<SGCharge>> sgChargeMap = getSgChargeMap(userIds, des);
            infoListVos = salesmanList.stream()
                    .map(salesman -> SalesmanInfoListVo.getSalesmanInfoListVo(salesman, salesmanUserMap.get(salesman.getId()), chargeMap, sgChargeMap, userMap))
                    .collect(Collectors.toList());
        } else {
            infoListVos = salesmanList.stream().map(salesman -> SalesmanInfoListVo.getSalesmanInfoListVo(salesman, null, null, null, null)).collect(Collectors.toList());
        }
        return new Page<SalesmanInfoListVo>()
                .setCurrent(salesmanPage.getCurrent())
                .setSize(salesmanPage.getSize())
                .setRecords(infoListVos)
                .setTotal(salesmanPage.getTotal())
                .setPages(salesmanPage.getPages());
    }

    private Map<Long, List<SGCharge>> getSgChargeMap(List<Long> userIds, List<String> des) {
        LambdaQueryWrapper<SGCharge> sgChargeWrapper = Wrappers.lambdaQuery(SGCharge.class)
                .in(SGCharge::getUid, userIds)
                .eq(SGCharge::getStatus, ChargeStatus.chain_success)
                .in(SGCharge::getToken, ListUtil.of(CurrencyCoinEnum.usdt.name(), CurrencyCoinEnum.usdc.name()))
                .in(SGCharge::getCharge_type, des);
        List<SGCharge> sgChargeList = sgChargeService.list(sgChargeWrapper);
        Map<Long, List<SGCharge>> sgChargeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(sgChargeList)) {
            sgChargeMap = sgChargeList.stream().collect(Collectors.groupingBy(SGCharge::getUid));
        }
        return sgChargeMap;
    }

    private Map<Long, List<Charge>> getChargeMap(List<Long> userIds, List<String> des) {
        List<Charge> chargeList = chargeService.list(Wrappers.lambdaQuery(Charge.class)
                .in(Charge::getUid, userIds)
                .eq(Charge::getStatus, ChargeStatus.chain_success)
                .in(Charge::getCharge_type, des));
        Map<Long, List<Charge>> chargeMap = new HashMap<>();
        if (CollUtil.isNotEmpty(chargeList)) {
            chargeMap = chargeList.stream().collect(Collectors.groupingBy(Charge::getUid));
        }
        return chargeMap;
    }

    private void setPurview(Long id, AdminAndRoles my, LambdaQueryWrapper<Salesman> wrapper) {
        String roleName = my.getRole().getName();
        if ((SalesmanEnum.业务员.name().equals(roleName) || StrUtil.equals(SalesmanEnum.业务主管.name(), roleName)) && ObjectUtil.isNotNull(id)) {
            Salesman salesman = this.getById(id);
            if (ObjectUtil.isNotNull(salesman) && (ObjectUtil.notEqual(id, salesman.getId()) && ObjectUtil.notEqual(id, salesman.getP_id()))) {
                throw ErrorCodeEnum.ACCESS_DENY.generalException();
            }
            wrapper.eq(Salesman::getId, id)
                    .or()
                    .eq(Salesman::getP_id, id);
        } else if ((SalesmanEnum.业务员.name().equals(roleName) || StrUtil.equals(SalesmanEnum.业务主管.name(), roleName)) && ObjectUtil.isNull(id)) {
            Salesman salesman = this.getOne(Wrappers.lambdaQuery(Salesman.class).eq(Salesman::getAdmin_id, my.getId()));
            wrapper.eq(Salesman::getId, salesman.getId())
                    .or()
                    .eq(Salesman::getP_id, salesman.getId());
        } else if (!(SalesmanEnum.业务员.name().equals(roleName) || StrUtil.equals(SalesmanEnum.业务主管.name(), roleName)) && ObjectUtil.isNotNull(id)) {
            wrapper.eq(Salesman::getId, id)
                    .or()
                    .eq(Salesman::getP_id, id);
        }
    }
}
