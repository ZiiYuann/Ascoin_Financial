package com.tianli.management.channel.service;

import cn.hutool.core.collection.CollUtil;
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
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.admin.AdminUpdatePwdDTO;
import com.tianli.management.admin.UpdateAdminDTO;
import com.tianli.management.channel.dao.ChannelMapper;
import com.tianli.management.channel.dto.ChannelEditDto;
import com.tianli.management.channel.entity.Channel;
import com.tianli.management.channel.entity.ChannelUser;
import com.tianli.management.channel.enums.ChannelRoleEnum;
import com.tianli.management.channel.vo.ChannelInfoListVo;
import com.tianli.management.channel.vo.ChannelLeaderListVo;
import com.tianli.management.channel.vo.ChannelListVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author lzy
 * @date 2022/5/7 11:35
 */
@Service
public class ChannelService extends ServiceImpl<ChannelMapper, Channel> {

    @Resource
    AdminService adminService;

    @Resource
    ChannelUserService channelUserService;


    @Transactional(rollbackFor = Exception.class)
    public void edit(ChannelEditDto channelEditDto) {
        Long id = channelEditDto.getId();
        Channel channel;
        if (ObjectUtil.isNotNull(id)) {
            channel = getUpdateChannel(channelEditDto, id);
        } else {
            channel = getInsertChannel(channelEditDto);
        }
        if (ObjectUtil.isNotNull(channelEditDto.getLeader_id()) && channel.getId().equals(channelEditDto.getLeader_id())) {
            ErrorCodeEnum.throwException("不能设置自己为上级渠道");
        }
        this.saveOrUpdate(channel);
    }

    private Channel getInsertChannel(ChannelEditDto channelEditDto) {
        Channel channel;
        AdminAndRoles my = adminService.my();
        Long adminId = adminService.createAdmin(channelEditDto.getCreateAdminDTO());
        channel = Channel.builder()
                .id(CommonFunction.generalId())
                .admin_id(adminId)
                //.creator("测试")
                //.creator_id(111L)
                .creator(my.getUsername())
                .creator_id(my.getId())
                .admin_username(channelEditDto.getUsername())
                .remark(channelEditDto.getRemark())
                .create_time(LocalDateTime.now())
                .p_id(channelEditDto.getLeader_id())
                .is_deleted(Boolean.FALSE)
                .build();
        return channel;
    }

    private Channel getUpdateChannel(ChannelEditDto channelEditDto, Long id) {
        Channel channel;
        channel = this.getById(id);
        if (ObjectUtil.isNull(channel)) {
            ErrorCodeEnum.throwException("渠道不存在");
        }
        if (StrUtil.isNotBlank(channelEditDto.getPassword())) {
            adminService.updatePwd(channel.getAdmin_id(), AdminUpdatePwdDTO.builder().password(channelEditDto.getPassword()).build());
        }
        if (ObjectUtil.isNotNull(channelEditDto.getLeader_id())) {
            Channel leaderChannel = this.getById(channelEditDto.getLeader_id());
            if (ObjectUtil.isNull(leaderChannel) || ObjectUtil.isNotNull(leaderChannel.getP_id())) {
                ErrorCodeEnum.throwException("上级渠道不存在");
            }
        } else {
            channel.setP_id(null);
        }
        Admin admin = adminService.getById(channel.getAdmin_id());
        admin.setUsername(channelEditDto.getUsername());
        this.updateAdmin(admin);
        channel.setAdmin_username(channelEditDto.getUsername());
        channel.setUpdate_time(LocalDateTime.now()).setRemark(channelEditDto.getRemark());
        return channel;
    }

    private void updateAdmin(Admin admin) {
        UpdateAdminDTO updateAdminDTO = new UpdateAdminDTO();
        updateAdminDTO.setId(admin.getId());
        updateAdminDTO.setUsername(admin.getUsername());
        updateAdminDTO.setNickname(admin.getNickname());
        updateAdminDTO.setPhone(admin.getPhone());
        updateAdminDTO.setNote(admin.getNote());
        updateAdminDTO.setRole_name(ChannelRoleEnum.渠道.name());
        adminService.updateAdmin(updateAdminDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteChannel(Long id) {
        Channel channel = this.getById(id);
        if (ObjectUtil.isNull(channel)) {
            return;
        }
        if (ObjectUtil.isNull(channel.getP_id())) {
            List<Channel> channelList = this.findByPId(channel.getId());
            if (CollUtil.isNotEmpty(channelList) && channelList.size() > 0) {
                ErrorCodeEnum.throwException("请先删除下级渠道");
            }
        }
        adminService.deleteAdmin(channel.getAdmin_id());
        channelUserService.remove(Wrappers.lambdaQuery(ChannelUser.class).eq(ChannelUser::getChannel_id, channel.getId()));
        this.removeById(channel.getId());
    }

    public List<Channel> findByPId(Long pid) {
        return this.list(Wrappers.lambdaQuery(Channel.class).eq(Channel::getP_id, pid));
    }

    public List<ChannelLeaderListVo> leaderList() {
        List<ChannelLeaderListVo> channelLeaderListVoList = new ArrayList<>();
        List<Channel> channels = this.list(Wrappers.lambdaQuery(Channel.class)
                .isNull(Channel::getP_id)
                .orderByDesc(Channel::getId));
        if (CollUtil.isNotEmpty(channels)) {
            channelLeaderListVoList = channels.stream().map(ChannelLeaderListVo::collect).collect(Collectors.toList());
        }
        return channelLeaderListVoList;
    }

    public IPage<ChannelListVo> channelList(Integer page, Integer size, String username) {
        LambdaQueryWrapper<Channel> wrapper = Wrappers.lambdaQuery(Channel.class).orderByDesc(Channel::getId);
        if (StrUtil.isNotBlank(username)) {
            wrapper.like(StrUtil.isNotBlank(username), Channel::getAdmin_username, username);
        } else {
            wrapper.isNull(Channel::getP_id);
        }
        Page<Channel> channelPage = this.page(new Page<>(page, size), wrapper);
        List<Channel> channels = channelPage.getRecords();
        if (CollUtil.isEmpty(channels)) {
            return new Page<>(page, size);
        }
        List<Long> channelIds = channels.stream().filter(channel -> ObjectUtil.isNull(channel.getP_id())).map(Channel::getId).collect(Collectors.toList());
        Map<Long, List<Channel>> teamChannelMap = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(channelIds)) {
            List<Channel> teamChannelList = this.list(Wrappers.lambdaQuery(Channel.class)
                    .in(Channel::getP_id, channelIds)
                    .orderByDesc(Channel::getId));
            if (CollUtil.isNotEmpty(teamChannelList)) {
                teamChannelMap = teamChannelList.stream().collect(Collectors.groupingBy(Channel::getP_id));
            }
        }
        Map<Long, List<Channel>> finalTeamChannelMap = teamChannelMap;
        List<ChannelListVo> result = channels.stream().map(channel -> ChannelListVo.collect(channel, finalTeamChannelMap.get(channel.getId()))).collect(Collectors.toList());
        return new Page<ChannelListVo>()
                .setCurrent(channelPage.getCurrent())
                .setSize(channelPage.getSize())
                .setRecords(result)
                .setTotal(channelPage.getTotal())
                .setPages(channelPage.getPages());
    }

    public IPage<ChannelInfoListVo> infoList(Integer page, Integer size, Long id) {
        LambdaQueryWrapper<Channel> wrapper = Wrappers.lambdaQuery(Channel.class);
        if (ObjectUtil.isNotNull(id)) {
            wrapper.eq(Channel::getId, id).or().eq(Channel::getP_id, id);
        }
        wrapper.orderByDesc(Channel::getId);
        Page<Channel> channelPage = this.page(new Page<>(page, size), wrapper);
        List<Channel> channels = channelPage.getRecords();
        if (CollUtil.isEmpty(channels)) {
            return new Page<>(page, size);
        }
        List<Long> pChannelIds = channels.stream().map(Channel::getP_id).filter(ObjectUtil::isNotNull).collect(Collectors.toList());
        Map<Long, Channel> pChannelMap = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(pChannelIds)) {
            List<Channel> pChannelList = this.list(Wrappers.lambdaQuery(Channel.class)
                    .in(Channel::getId, pChannelIds)
                    .orderByDesc(Channel::getId));
            if (CollUtil.isNotEmpty(pChannelList)) {
                pChannelMap = pChannelList.stream().collect(Collectors.toMap(Channel::getId, Function.identity(), (v1, v2) -> v1));
            }
        }
        Map<Long, Channel> finalPChannelMap = pChannelMap;
        List<ChannelInfoListVo> result = channels.stream().map(channel -> ChannelInfoListVo.collect(channel, finalPChannelMap.get(channel.getP_id()))).collect(Collectors.toList());
        return new Page<ChannelInfoListVo>()
                .setCurrent(channelPage.getCurrent())
                .setSize(channelPage.getSize())
                .setRecords(result)
                .setTotal(channelPage.getTotal())
                .setPages(channelPage.getPages());
    }
}
