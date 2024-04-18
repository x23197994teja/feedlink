package com.teja.feedlink.model;

import lombok.Data;

@Data
public class ResponseModel {
    private String status;
    private String message;
    private String creationId;
    private String exception;
}

