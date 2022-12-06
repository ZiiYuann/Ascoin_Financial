package com.tianli.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-05
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteDTO {

    private Long uid;

    private Long chatId;

    private List<InviteDTO> list;
}
