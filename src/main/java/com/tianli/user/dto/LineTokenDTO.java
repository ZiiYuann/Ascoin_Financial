package com.tianli.user.dto;

import lombok.Data;

@Data
public class LineTokenDTO {
    /**
     * Access token. Valid for 30 days.
     */
    private String access_token;

    /**
     * Number of seconds until the access token expires.
     */
    private String expires_in;

    /**
     *
     */
    private String id_token;

    /**
     *
     */
    private String refresh_token;

    /**
     *
     */
    private String scope;

    /**
     *
     */
    private String token_type;
}
