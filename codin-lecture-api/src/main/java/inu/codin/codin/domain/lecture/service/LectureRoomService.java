package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.lecture.dto.LectureRoomResponseDto;
import inu.codin.codin.domain.lecture.entity.LectureRoom;
import inu.codin.codin.domain.lecture.repository.LectureRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LectureRoomService {

    private final LectureRoomRepository lectureRoomRepository;

    /**
     * List 인덱스마다 층고를 뜻하며, 각각 강의실마다 진행되는 강의 스케줄을 Map 형식으로 관리
     * @return List<Map<강의실 호수, 해당 강의실에서 진행되는 강의 스케줄>>
     */
    public List<Map<Integer, List<LectureRoomResponseDto>>> statusOfEmptyRoom() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        List<LectureRoom> lectureRooms = lectureRoomRepository.findAllWithSchedules();

        List<Map<Integer, List<LectureRoomResponseDto>>> statusOfRooms = new ArrayList<>(); //배열 인덱스마다 층고를 뜻함

        for (int floor = 1; floor <= 5; floor++) { //1번 인덱스 = 1층 강의실
            Map<Integer, List<LectureRoomResponseDto>> floorMap = new HashMap<>(); // 강의실 호수 : 해당 호수에서 진행되는 강의 스케줄
            for (LectureRoom lr : lectureRooms){
                int room = lr.getRoomNum();
                if ((room / 100) == floor) {
                    List<LectureRoomResponseDto> emptyRooms = lr.getSchedules().stream()
                                    .filter(schedule -> schedule.getDayOfWeek().equals(today))
                                    .map(schedule -> LectureRoomResponseDto.of(schedule.getLecture(), room, schedule))
                                    .toList();
                    floorMap.put(room, emptyRooms);
                }
            }
            statusOfRooms.add(floorMap);
        }
        return statusOfRooms;
    }
}
