package com.example.meeting_schedule.global.exception;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String code;
    private final String message;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
