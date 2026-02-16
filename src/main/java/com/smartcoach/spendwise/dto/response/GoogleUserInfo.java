package com.smartcoach.spendwise.dto.response;


import lombok.Data;

@Data

public class GoogleUserInfo {

    private String sub;
    private String email;
    private String name;
    private boolean emailVerified;

}
