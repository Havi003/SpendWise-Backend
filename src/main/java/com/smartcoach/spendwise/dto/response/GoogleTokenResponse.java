package com.smartcoach.spendwise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class GoogleTokenResponse {

    private String accessToken;
    private String idToken;
    private Long expiresIn;
    private String tokenType;


}
