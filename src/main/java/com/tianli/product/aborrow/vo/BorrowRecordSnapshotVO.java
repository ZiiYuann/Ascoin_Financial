package com.tianli.product.aborrow.vo;

import com.tianli.product.aborrow.dto.BorrowRecordSnapshotDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-20
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordSnapshotVO {

    private BorrowRecordSnapshotDTO borrowRecordSnapshotDTO;

    private BorrowRecordVO borrowRecordVO;
}
