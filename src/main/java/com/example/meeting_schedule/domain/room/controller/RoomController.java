package com.example.meeting_schedule.domain.room.controller;

import com.example.meeting_schedule.domain.room.dto.RoomRequest;
import com.example.meeting_schedule.domain.room.dto.RoomResponse;
import com.example.meeting_schedule.domain.room.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    // 방 생성
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    // 방 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable("roomId") String roomId) {
        return ResponseEntity.ok(roomService.getRoom(roomId));
    }
}
