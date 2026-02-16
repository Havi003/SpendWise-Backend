package com.smartcoach.spendwise.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsWebhookRequest {

    //ie from mpesa
    private String from;

    //message content ie you have recieved 1000 from Kingangi
    private String message;

}
