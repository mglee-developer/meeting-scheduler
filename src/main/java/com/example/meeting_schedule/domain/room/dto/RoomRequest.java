package com.example.meeting_schedule.domain.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class RoomRequest {
    @NotBlank(message = "모임 이름은 필수입니다.")
    private String title;

    @NotBlank(message = "방장 이름은 필수입니다.")
    private String hostName;

    @NotNull(message = "시작 날짜는 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료 날짜는 필수입니다.")
    private LocalDate endDate;
}
