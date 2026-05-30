package com.example.meeting_schedule.domain.room.dto;

import com.example.meeting_schedule.domain.participant.dto.ParticipantResponse;
import com.example.meeting_schedule.domain.participant.entity.Participant;
import com.example.meeting_schedule.domain.room.entity.Room;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class RoomResponse {
    private String id;
    private String title;
    private String hostName;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<ParticipantResponse> participants;

    public RoomResponse(Room room, List<ParticipantResponse> participants) {
        this.id = room.getId();
        this.title = room.getTitle();
        this.hostName = room.getHostName();
        this.startDate = room.getStartDate();
        this.endDate = room.getEndDate();
        this.participants = participants;
    }
}
