package com.smartcoach.spendwise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WsResponse<T> {
    private WsHeader header;
    private T body;
}