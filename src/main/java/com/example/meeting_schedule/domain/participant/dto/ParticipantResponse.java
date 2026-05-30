package com.example.meeting_schedule.domain.participant.dto;

import com.example.meeting_schedule.domain.participant.entity.AvailableDate;
import com.example.meeting_schedule.domain.participant.entity.Participant;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ParticipantResponse {
    private String name;
    private List<AvailableResponse> availableDates;

    public ParticipantResponse(Participant participant, List<AvailableDate> availableDates) {
        this.name = participant.getName();
        this.availableDates = availableDates.stream()
                .map(a -> new AvailableResponse(a.getDate()))
                .collect(Collectors.toList());
    }
}
