package com.example.meeting_schedule.domain.participant.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ResultResponse {
    private int totalParticipants;
    private List<ResultDateResponse> results;
}
