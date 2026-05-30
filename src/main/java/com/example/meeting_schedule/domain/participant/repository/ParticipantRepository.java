package com.example.meeting_schedule.domain.participant.repository;

import com.example.meeting_schedule.domain.participant.entity.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    List<Participant> findByRoom_Id(String roomId);
    boolean existsByRoom_IdAndName(String roomId, String name);
    Optional<Participant> findByRoom_IdAndName(String roomId, String name);
}
