package com.smartcoach.spendwise.components;

import org.springframework.stereotype.Component;

import com.smartcoach.spendwise.dto.response.WsHeader;
import com.smartcoach.spendwise.dto.response.WsResponse;

@Component
public class WsResponseMapper {

        public <T> WsResponse<T> success(String message, T body) {
        return new WsResponse<>(
            new WsHeader("200", message),
            body
        );
    }

    public <T> WsResponse<T> error(String code, String message) {
        return new WsResponse<>(
            new WsHeader(code, message),
            null
        );
    }
}
