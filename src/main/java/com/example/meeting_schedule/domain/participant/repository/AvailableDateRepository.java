package com.example.meeting_schedule.domain.participant.repository;

import com.example.meeting_schedule.domain.participant.entity.AvailableDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailableDateRepository extends JpaRepository<AvailableDate, Long> {
    List<AvailableDate> findByParticipant_Id(Long participantId);
    List<AvailableDate> findByParticipant_Room_Id(String roomId);

    void deleteByParticipant_Id(Long participantId);
}
