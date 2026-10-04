package com.example.meeting_schedule.domain.participant.repository;

import com.example.meeting_schedule.domain.participant.entity.AvailableDate;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AvailableDateRepository extends JpaRepository<AvailableDate, Long> {
    @Query("""
            SELECT ad
            FROM AvaliableDate ad
            JOIN FETCH ad.participant p
            WHERE p.room.id = :roomId
            """)
    List<AvailableDate> findByParticipant_Room_Id(@Param("roomId") String roomId);

    void deleteByParticipant_Id(Long participantId);
}
