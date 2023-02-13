package com.tianli.accountred.convert;

import com.tianli.accountred.dto.RedEnvelopeSpiltDTO;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.query.RedEnvelopeIoUQuery;
import com.tianli.accountred.vo.RedEnvelopeExternGetDetailsVO;
import com.tianli.accountred.vo.RedEnvelopeGetDetailsVO;
import com.tianli.accountred.vo.RedEnvelopeGiveRecordVO;
import com.tianli.accountred.vo.RedEnvelopeSpiltGetRecordVO;
import org.mapstruct.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@Mapper(componentModel = "spring")
public interface RedEnvelopeConvert {

    RedEnvelope toDO(RedEnvelopeIoUQuery query);

    RedEnvelopeGiveRecordVO toRedEnvelopeGiveRecordVO(RedEnvelope redEnvelope);

    RedEnvelopeSpiltGetRecordVO toRedEnvelopeSpiltGetRecordVO(RedEnvelopeSpiltGetRecord redEnvelopeSpiltGetRecord);

    RedEnvelopeGetDetailsVO toRedEnvelopeGetDetailsVO(RedEnvelope redEnvelope);

    RedEnvelopeSpiltDTO toRedEnvelopeSpiltDTO(RedEnvelopeSpilt redEnvelopeSpilt);

    RedEnvelopeExternGetDetailsVO toRedEnvelopeExternGetDetailsVO(RedEnvelope redEnvelope);

}
