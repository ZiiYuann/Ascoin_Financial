package com.tianli.operate;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.operate.mapper.Operate;
import com.tianli.operate.mapper.OperateConf;
import com.tianli.operate.mapper.OperateMapper;
import com.tianli.operate.mapper.OperateVO;
import com.tianli.operate.operateType.OperateTypeService;
import com.tianli.operate.operateType.mapper.OperateType;
import com.tianli.operate.otc.SaveOperateTransDTO;
import com.tianli.operate.otc.UpdateOperateTransDTO;
import com.tianli.tool.MapTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 运营位配置表
 *
 * @author linyifan
 *  2/24/21 2:59 PM
 */

@Service
public class OperateService extends ServiceImpl<OperateMapper, Operate> {

    /**
     * 查询
     * @param location 页面位置
     * @param type     图片类型
     * @param page 页码
     * @param size 每页大小
     */
    public Map<String, Object> operatePage(Integer location, Integer type, Integer page, Integer size){
        //根据页面标识location查询 operateType
        List<OperateType> list = operateTypeService.list(new LambdaQueryWrapper<OperateType>().eq(OperateType::getPage_location, location));
        if(CollectionUtils.isEmpty(list)){
            return Maps.newHashMap();
        }
        //根据operateType获取对应的operate_id
        List<Long> operateIdList = list.stream().map(OperateType::getOperate_id).collect(Collectors.toList());
        //根据类型、上线状态、符合条件的id 查询operate
        LambdaQueryWrapper<Operate> wrapper = new LambdaQueryWrapper<Operate>()
                .in(Operate::getId, operateIdList)
                .eq(Operate::getType, type)
                .orderByAsc(Operate::getSort)
                .orderByDesc(Operate::getCreate_time);
        Page<Operate> operatePage = super.page(new Page<>(page, size), wrapper);
        List<Operate> operateList = operatePage.getRecords();
        if (CollectionUtils.isEmpty(operateList)) return Maps.newHashMap();
        //根据查询到的operateList获取对应operate_id集合
        List<Long> selectIdList = operateList.stream().map(Operate::getId).collect(Collectors.toList());
        //根据operate_id集合查询operate_type表
        List<OperateType> selectOperateTypeList = operateTypeService.list(new LambdaQueryWrapper<OperateType>().in(OperateType::getOperate_id, selectIdList));
        //获取operate_id : {operateType}
        Map<Long, List<OperateType>> collect = selectOperateTypeList.stream().collect(Collectors.groupingBy(OperateType::getOperate_id));
        String operate_config_list = configService.getOrDefaultNoCache("operate_config_list", "{\"location\":[],\"opType\":[]}");
        Map<Integer, String> typaNameMap = new Gson().fromJson(operate_config_list, OperateConf.class).getLocation().stream().collect(Collectors.toMap(OperateConf.OperateItem::getType, OperateConf.OperateItem::getName));
        List<OperateVO> vos = operateList.stream().map(e -> {
            //根据operate -> operateVO(此时id、locations为null)
            OperateVO trans = OperateVO.trans(e);
            //根据operate_id 获取对应的operateType集合，进而获取页面标识集合
            List<OperateType> operateTypes = collect.get(trans.getOperateId());
            //一条operate -> 多个页面，用集合locations表示
            trans.setLocations(operateTypes.stream().map(OperateType::getPage_location).collect(Collectors.toList()));
            List<String> strings = operateTypes.stream().map(ee -> typaNameMap.get(ee.getPage_location())).collect(Collectors.toList());
            trans.setLocationsDes(String.join(",", strings));
            trans.setLocation(operateTypes.get(0).getPage_location());
            trans.setId(operateTypes.get(0).getId());
            return trans;
        }).collect(Collectors.toList());
        return MapTool.Map().put("list", vos).put("count", operatePage.getTotal());
    }

    /**
     * 返回给客户端
     */
    public List<OperateVO> operateClientPage(Integer location, Integer type){
        //根据页面标识location查询 operateType
        List<OperateType> list = operateTypeService.list(new LambdaQueryWrapper<OperateType>().eq(OperateType::getPage_location, location));
        if(CollectionUtils.isEmpty(list)){
            return Lists.newArrayList();
        }
        //根据operateType获取对应的operate_id
        List<Long> operateIdList = list.stream().map(OperateType::getOperate_id).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(operateIdList)) return Lists.newArrayList();
        //根据类型、上线状态、符合条件的id 查询operate
        LambdaQueryWrapper<Operate> wrapper = new LambdaQueryWrapper<Operate>()
                .in(Operate::getId, operateIdList)
                .eq(Operate::getType, type)
                .eq(Operate::getOnline, Boolean.TRUE)
                .orderByAsc(Operate::getSort)
                .orderByDesc(Operate::getCreate_time);
        //Page<Operate> operatePage = super.page(new Page<>(page, size), wrapper);
        //List<Operate> operateList = operatePage.getRecords();
        List<Operate> operateList = super.list(wrapper);
        if (CollectionUtils.isEmpty(operateList)) return Lists.newArrayList();
        //根据查询到的operateList获取对应operate_id集合
        List<Long> selectIdList = operateList.stream().map(Operate::getId).collect(Collectors.toList());
        //根据operate_id集合查询operate_type表
        List<OperateType> selectOperateTypeList = operateTypeService.list(new LambdaQueryWrapper<OperateType>().in(OperateType::getOperate_id, selectIdList));
        //获取operate_id : {operateType}
        Map<Long, List<OperateType>> collect = selectOperateTypeList.stream().collect(Collectors.groupingBy(OperateType::getOperate_id));
        return operateList.stream().map(e -> {
            //根据operate -> operateVO(此时id、locations为null)
            OperateVO trans = OperateVO.trans(e);
            //根据operate_id 获取对应的operateType集合，进而获取页面标识集合
            List<OperateType> operateTypes = collect.get(trans.getOperateId());
            //一条operate -> 多个页面，用集合locations表示
            trans.setLocations(operateTypes.stream().map(OperateType::getPage_location).collect(Collectors.toList()));
            trans.setLocation(operateTypes.get(0).getPage_location());
            trans.setId(operateTypes.get(0).getId());
            return trans;
        }).collect(Collectors.toList());
    }

    /**
     * 返回给客户端(旧)
     */
    public List<OperateVO> operateClientList(Integer location, Integer type, Integer page, Integer size){
        return operateMapper.getClientPageList(location, type, Math.max((page - 1) * size, 0), size, System.currentTimeMillis());
    }

    public Integer operateCount(Integer location,Integer type){
        return operateMapper.countOperates(location,type);
    }

    /**
     * 获取最大sort
     */
    public Integer getMaxSort(){
        return operateMapper.getMaxSort();
    }

    @Transactional
    public Operate saveOperate(SaveOperateTransDTO operateDTO){
        if (Objects.isNull(operateDTO.getSort())) operateDTO.setSort(getMaxSort()+1);
        Operate operate = Operate.builder()
                .id(CommonFunction.generalId())
                .sort(operateDTO.getSort())
                .picture(operateDTO.getPicture())
                .online(operateDTO.getOnline())
                .url(operateDTO.getUrl())
                .start_time(operateDTO.getStart_time())
                .end_time(operateDTO.getEnd_time())
                .create_time(LocalDateTime.now())
                .validity(operateDTO.getValidity()).build();
        if (!super.save(operate)) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        List<Integer> locationList;
        if(!CollectionUtils.isEmpty(operateDTO.getLocations())){
            locationList = operateDTO.getLocations();
        }else{
            locationList = Lists.newArrayList(operateDTO.getLocation());
        }
        //批量添加operateType 1个operate_id -> 多个location
        operateTypeService.saveOperateType(operate.getId(), locationList);
        return operate;
    }

    @Transactional
    public void updateOperate(UpdateOperateTransDTO operateDTO){
        if (Objects.isNull(operateDTO.getSort())) operateDTO.setSort(getMaxSort()+1);
        Long operateId = operateDTO.getOperateId();
        Operate update = Operate.builder()
                .id(operateId)
                .sort(operateDTO.getSort())
                .picture(operateDTO.getPicture())
                .online(operateDTO.getOnline())
                .url(operateDTO.getUrl())
                .start_time(operateDTO.getStart_time())
                .end_time(operateDTO.getEnd_time())
                .validity(operateDTO.getValidity()).build();
        boolean updateById = super.updateById(update);
        if (!updateById) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        //更新type的关联数据 删除新增方案
        boolean remove = operateTypeService.remove(new LambdaQueryWrapper<OperateType>().eq(OperateType::getOperate_id, operateId));
        if (!remove) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        List<Integer> locationList;
        if(!CollectionUtils.isEmpty(operateDTO.getLocations())){
            locationList = operateDTO.getLocations();
        }else{
            locationList = Lists.newArrayList(operateDTO.getLocation());
        }
        operateTypeService.saveOperateType(operateId, locationList);
    }


    @Resource
    private OperateMapper operateMapper;

    @Resource
    private OperateTypeService operateTypeService;

    @Resource
    private ConfigService configService;
}
