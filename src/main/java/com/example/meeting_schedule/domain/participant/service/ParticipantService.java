package com.example.meeting_schedule.domain.participant.service;

import com.example.meeting_schedule.domain.participant.dto.ParticipantRequest;
import com.example.meeting_schedule.domain.participant.dto.ParticipantResponse;
import com.example.meeting_schedule.domain.participant.dto.ResultDateResponse;
import com.example.meeting_schedule.domain.participant.dto.ResultResponse;
import com.example.meeting_schedule.domain.participant.entity.AvailableDate;
import com.example.meeting_schedule.domain.participant.entity.Participant;
import com.example.meeting_schedule.domain.participant.repository.AvailableDateRepository;
import com.example.meeting_schedule.domain.participant.repository.ParticipantRepository;
import com.example.meeting_schedule.domain.room.entity.Room;
import com.example.meeting_schedule.domain.room.repository.RoomRepository;
import com.example.meeting_schedule.global.exception.ErrorCode;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipantService {
    private final RoomRepository roomRepository;
    private final ParticipantRepository participantRepository;
    private final AvailableDateRepository availableDateRepository;
    private final RedisTemplate redisTemplate;

    @Transactional
    public ParticipantResponse saveAvaliability(String roomId, ParticipantRequest request) {
        Room room = findActiveRoom(roomId);

        for(LocalDate date : request.getAvailableDates()) {
            if(date.isBefore(room.getStartDate()) ||  date.isAfter(room.getEndDate())) {
                throw ErrorCode.INVALID_DATE.toException();
            }
        }

        boolean idDuplicated = participantRepository.existsByRoom_IdAndName(roomId, request.getParticipantName());

        Participant saved = idDuplicated
                ? participantRepository.findByRoom_IdAndName(roomId, request.getParticipantName())
                    .orElseThrow(ErrorCode.PARTICIPANT_NOT_FOUND::toException)
                : participantRepository.save(Participant.builder()
                                .room(room)
                                .name(request.getParticipantName())
                                .build());
        if(idDuplicated) {
            availableDateRepository.deleteByParticipant_Id(saved.getId());
        }

        List<AvailableDate> dates = request.getAvailableDates().stream()
                .map(date -> AvailableDate.builder()
                        .participant(saved)
                        .date(date)
                        .build())
                .collect(Collectors.toList());
        availableDateRepository.saveAll(dates);

        return new ParticipantResponse(saved, dates);
    }

    @Transactional(readOnly = true)
    public ResultResponse getResult(String roomId) {
        Room room = findActiveRoom(roomId);

        List<Participant> participants = participantRepository.findByRoom_Id(roomId);
        if(participants.isEmpty()) {
            return null;
        }
        // 방에 속한 모든 참여자의 가능한 날짜를 한 번에 조회
        List<AvailableDate> availableDates =
                availableDateRepository.findByParticipant_Room_Id(roomId);

        // Map<날짜, 가능한 참여자 이름 목록>
        Map<LocalDate, List<String>> dateMap = new HashMap<>();

        availableDates.forEach(availableDate -> {
            dateMap.computeIfAbsent(
                    availableDate.getDate(),
                    key -> new ArrayList<>()
            ).add(availableDate.getParticipant().getName());
        });

        List<ResultDateResponse> results = dateMap.entrySet().stream()
                .map(entry -> ResultDateResponse.builder()
                        .date(entry.getKey())
                        .avilableCount(entry.getValue().size())
                        .availableParticipants(entry.getValue())
                        .allAvailable(entry.getValue().size() == participants.size())
                        .build())
                .sorted(Comparator
                        .comparing(ResultDateResponse::isAllAvailable)
                        .reversed()
                        .thenComparing(
                                ResultDateResponse::getAvilableCount,
                                Comparator.reverseOrder()
                        ))
                .collect(Collectors.toList());

        return ResultResponse.builder()
                .totalParticipants(participants.size())
                .results(results)
                .build();
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
