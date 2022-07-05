package com.tianli.user.dto;

import lombok.Data;

@Data
public class LineCallbackDTO {
    /**
     * Authorization code used to get an access token. Valid for 10 minutes. This authorization code can only be used once
     */
    private String code;

    /**
     * Authorization code used to get an access token. Valid for 10 minutes. This authorization code can only be used once
     */
    private String accessToken;

    /**
     * A unique alphanumeric string used to prevent cross-site request forgery (opens new window).
     * Verify that this matches the value of the state parameter given to the authorization URL.
     */
    private String state;

    /**
     * This parameter does not appear in the in-app browser of iOS and Android apps. We are currently working to resolve this issue.
     */
    private String error_description;

    /**
     * DISALLOWED
     */
    private String errorMessage;

    /**
     * 417
     */
    private String errorCode;

    /**
     * access_denied
     */
    private String error;
}
