package com.tianli.other.convert;

import com.tianli.other.entity.Banner;
import com.tianli.other.query.BannerIoUQuery;
import com.tianli.other.vo.BannerVO;
import com.tianli.other.vo.MBannerVO;
import org.mapstruct.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-12
 **/
@Mapper(componentModel = "spring")
public interface OtherConvert {

    Banner toDO(BannerIoUQuery bannerIoUQuery);

    MBannerVO toMBannerVO(Banner banner);

    BannerVO toBannerVO(Banner banner);
}
