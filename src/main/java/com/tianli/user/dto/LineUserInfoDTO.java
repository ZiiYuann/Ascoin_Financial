package com.tianli.user.dto;

import lombok.Data;

@Data
public class LineUserInfoDTO {
    /**
     * User ID for which the ID token was generated.
     * 唯一标识
     */
    private String userId;
    /**
     * line user name
     */
    private String displayName;

    /**
     * line user picture
     */
    private String pictureUrl;

}
