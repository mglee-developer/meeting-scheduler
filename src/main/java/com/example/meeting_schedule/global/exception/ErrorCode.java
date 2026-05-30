package com.example.meeting_schedule.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    ROOM_NOT_FOUND("ROOM_NOT_FOUND", "방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ROOM_EXPIRED("ROOM_EXPIRED", "만료된 방입니다.", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("INVALID_DATE_RANGE", "잘못된 날짜 범위입니다.", HttpStatus.BAD_REQUEST),
    PARTICIPANT_NOT_FOUND("PARTICIPANT_NOT_FOUND", "참여자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_PARTICIPANT("DUPLICATE_PARTICIPANT", "이미 참여한 이름입니다.", HttpStatus.CONFLICT),
    INVALID_DATE("INVALID_DATE", "방 날짜 범위가 잘못되었습니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public BusinessException toException() {
        return new BusinessException(code, message, status);
    }
}
