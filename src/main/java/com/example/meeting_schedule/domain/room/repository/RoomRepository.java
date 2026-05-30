package com.example.meeting_schedule.domain.room.repository;

import com.example.meeting_schedule.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, String> {

}
