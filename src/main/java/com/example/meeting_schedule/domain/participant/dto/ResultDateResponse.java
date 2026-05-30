package com.example.meeting_schedule.domain.participant.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ResultDateResponse {
    private LocalDate date;
    private int avilableCount;                  // 가능한 인원 수
    private List<String> availableParticipants; // 가능한 참여자 이름 목록
    private boolean allAvailable;               // 전언 가능 여부
}
