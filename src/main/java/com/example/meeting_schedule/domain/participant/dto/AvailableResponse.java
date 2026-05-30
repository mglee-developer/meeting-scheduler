package com.example.meeting_schedule.domain.participant.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class AvailableResponse {
    private LocalDate date;

    public AvailableResponse(LocalDate date) {
        this.date = date;
    }
}
