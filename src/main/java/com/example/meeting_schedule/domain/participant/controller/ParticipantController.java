package com.example.meeting_schedule.domain.participant.controller;

import com.example.meeting_schedule.domain.participant.dto.ParticipantRequest;
import com.example.meeting_schedule.domain.participant.dto.ParticipantResponse;
import com.example.meeting_schedule.domain.participant.dto.ResultResponse;
import com.example.meeting_schedule.domain.participant.service.ParticipantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class ParticipantController {
    private final ParticipantService participantService;
    // POST /api/rooms/{roomId}/availability
    // @PathVariable로 roomId 받기
    // @Valid로 요청 검증
    // 201 Created 반환
    @PostMapping("/{roomId}/availability")
    public ResponseEntity<ParticipantResponse> createParticipant(@PathVariable("roomId") String roomId, @Valid @RequestBody ParticipantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(participantService.saveAvaliability(roomId, request));
    }

    // GET /api/rooms/{roomId}/result
    // @PathVariable로 roomId 받기
    // 200 OK 반환
    @GetMapping("/{roomId}/result")
    public ResponseEntity<ResultResponse> getResult(@PathVariable("roomId") String roomId) {
        return ResponseEntity.ok(participantService.getResult(roomId));
    }
}
