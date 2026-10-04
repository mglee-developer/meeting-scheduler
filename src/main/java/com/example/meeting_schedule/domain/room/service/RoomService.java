package com.example.meeting_schedule.domain.room.service;

import com.example.meeting_schedule.domain.participant.dto.ParticipantResponse;
import com.example.meeting_schedule.domain.participant.entity.AvailableDate;
import com.example.meeting_schedule.domain.participant.entity.Participant;
import com.example.meeting_schedule.domain.participant.repository.AvailableDateRepository;
import com.example.meeting_schedule.domain.participant.repository.ParticipantRepository;
import com.example.meeting_schedule.domain.room.dto.RoomRequest;
import com.example.meeting_schedule.domain.room.dto.RoomResponse;
import com.example.meeting_schedule.domain.room.entity.Room;
import com.example.meeting_schedule.domain.room.repository.RoomRepository;
import com.example.meeting_schedule.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final AvailableDateRepository availableDateRepository;
    private final RedisTemplate redisTemplate;

    // 방 생성
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        LocalDate today = LocalDate.now();

        // endDate까지 남은 일수
        long daysUntilExpiry = ChronoUnit.DAYS.between(today, request.getEndDate());
        // 날짜 검증 추가
        if(daysUntilExpiry <= 0) {
            throw ErrorCode.INVALID_DATE_RANGE.toException();
        }

        Room room = Room.builder()
                .title(request.getTitle())
                .hostName(request.getHostName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        Room savedRoom = roomRepository.save(room);

        // Redis에 만료 시간 저장
        redisTemplate.opsForValue().set(
                "room:" + savedRoom.getId(),
                "active",
                daysUntilExpiry,
                TimeUnit.DAYS
        );

        // 방 생성 시, 참여자는 빈 리스트
        return new RoomResponse(savedRoom, List.of());
    }

    // 방 조회
    @Transactional(readOnly = true)
    public RoomResponse getRoom(String roomId) {

        Room room = findActiveRoom(roomId);
        List<Participant> participants =
                participantRepository.findByRoom_Id(room.getId());

        List<AvailableDate> availableDates =
                availableDateRepository.findByParticipant_Room_Id(room.getId());

        Map<Long, List<AvailableDate>> availableDatesByParticipant =
                availableDates.stream()
                        .collect(Collectors.groupingBy(
                                availableDate -> availableDate.getParticipant().getId()
                        ));

        List<ParticipantResponse> participantResponse = participants.stream()
                .map(participant -> new ParticipantResponse(
                        participant,
                        availableDatesByParticipant.getOrDefault(
                                participant.getId(),
                                List.of()
                        )
                ))
                .collect(Collectors.toList());

        return new RoomResponse(room, participantResponse);
    }

    // 방 조회, Redis 만료확인
    private Room findActiveRoom(String roomId) {
        // 만료된 방인지 확인
        boolean isActive = redisTemplate.opsForValue().get("room:" + roomId) != null;
        if(!isActive) {
            throw ErrorCode.ROOM_EXPIRED.toException();
        }

        return roomRepository.findById(roomId)
                .orElseThrow(ErrorCode.ROOM_NOT_FOUND::toException);
    }
}
