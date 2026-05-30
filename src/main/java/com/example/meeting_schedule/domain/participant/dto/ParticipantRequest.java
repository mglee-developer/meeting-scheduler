package com.example.meeting_schedule.domain.participant.dto;

import com.example.meeting_schedule.domain.participant.entity.AvailableDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
public class ParticipantRequest {
    @NotBlank(message = "참가자 이름은 필수입니다.")
    private String participantName;

    @NotEmpty
    private List<LocalDate> availableDates;

}
