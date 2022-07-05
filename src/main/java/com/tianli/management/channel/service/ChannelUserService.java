package com.tianli.management.channel.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.ExcelUtils;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.channel.dao.ChannelUserMapper;
import com.tianli.management.channel.entity.Channel;
import com.tianli.management.channel.entity.ChannelUser;
import com.tianli.management.channel.vo.ChannelCpaListVo;
import com.tianli.management.channel.vo.ChannelCpaStatisticsVo;
import org.apache.poi.ss.usermodel.Workbook;
import org.jeecgframework.poi.excel.ExcelExportUtil;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lzy
 * @date 2022/5/7 11:38
 */
@Service
public class ChannelUserService extends ServiceImpl<ChannelUserMapper, ChannelUser> {

    @Resource
    ChannelUserMapper channelUserMapper;

    @Resource
    ChannelService channelService;

    @Resource
    HttpServletResponse response;

    public IPage<ChannelCpaListVo> cpaList(Long channelId, String username, Integer kycStatus, String startTime, String endTime, Integer page, Integer size) {
        List<Long> channelIds = getChannelIds(channelId);
        Long count = channelUserMapper.cpaCount(channelIds, username, kycStatus, startTime, endTime);
        if (ObjectUtil.isNull(count) || count <= 0) {
            return new Page<>(page, size);
        }
        List<ChannelCpaListVo> cpaList = channelUserMapper.cpaList(channelIds, username, kycStatus, startTime, endTime, (page - 1) * size, size);
        return new Page<ChannelCpaListVo>(page, size)
                .setTotal(count)
                .setRecords(cpaList);
    }

    public List<Long> getChannelIds(Long channelId) {
        List<Long> channelIds = new ArrayList<>();
        if (ObjectUtil.isNotNull(channelId)) {
            Channel channel = channelService.getById(channelId);
            if (ObjectUtil.isNotNull(channel)) {
                List<Channel> teamChannels = channelService.findByPId(channelId);
                if (CollUtil.isNotEmpty(teamChannels)) {
                    teamChannels.forEach(teamChannel -> channelIds.add(teamChannel.getId()));
                }
                channelIds.add(channel.getId());
            }
        }
        return channelIds;
    }

    public ChannelCpaStatisticsVo statistics(Long channelId, String username, Integer kycStatus, String startTime, String endTime) {
        List<Long> channelIds = getChannelIds(channelId);
        Long register_count = channelUserMapper.cpaCount(channelIds, username, kycStatus, startTime, endTime);
        Long kyc_success_count;
        //默认查kyc通过总数的,如果传了kycStatus并且不是成功状态 那么kyc_success_count直接设置为0
        if (ObjectUtil.isNotNull(kycStatus) && !ObjectUtil.equal(kycStatus, 1)) {
            kyc_success_count = 0L;
        } else {
            kycStatus = 1;
            kyc_success_count = channelUserMapper.cpaCount(channelIds, username, kycStatus, startTime, endTime);
        }
        return ChannelCpaStatisticsVo.builder()
                .register_count(register_count)
                .kyc_success_count(kyc_success_count)
                .build();
    }

    public List<ChannelUser> findByChannelId(List<Long> channelIds) {
        return this.list(Wrappers.lambdaQuery(ChannelUser.class).in(ChannelUser::getChannel_id, channelIds));
    }

    public void export(Long channelId, String username, Integer kycStatus, String startTime, String endTime) {
        List<Long> channelIds = getChannelIds(channelId);
        List<ChannelCpaListVo> cpaList = channelUserMapper.cpaList(channelIds, username, kycStatus, startTime, endTime, null, null);
        if (CollUtil.isEmpty(cpaList)) {
            ErrorCodeEnum.throwException("没有需要导出的数据");
        }
        ExportParams entity = new ExportParams();
        entity.setSheetName("推广统计cpa");
        Workbook workbook = ExcelExportUtil.exportExcel(entity, ChannelCpaListVo.class, cpaList);
        try {
            ExcelUtils.export(workbook, response, "推广统计cpa");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
