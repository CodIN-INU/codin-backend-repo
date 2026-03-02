package inu.codin.lecture.domain.lecture.service;

import inu.codin.common.entity.College;
import inu.codin.lecture.domain.lecture.dto.LectureRoomResponseDto;
import inu.codin.lecture.domain.lecture.entity.LectureRoom;
import inu.codin.lecture.domain.lecture.repository.LectureRoomRepository;
import inu.codin.lecture.domain.user.dto.UserInfoResponse;
import inu.codin.lecture.domain.user.service.UserClientService;
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
    private final UserClientService userClientService;

    /**
     * List 인덱스마다 층고를 뜻하며, 각각 강의실마다 진행되는 강의 스케줄을 Map 형식으로 관리
     * @return List<Map<강의실 호수, 해당 강의실에서 진행되는 강의 스케줄>>
     */
    public List<Map<Integer, List<LectureRoomResponseDto>>> statusOfEmptyRoom() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        List<LectureRoom> lectureRooms = lectureRoomRepository.findAllWithSchedulesAndLectures();

        UserInfoResponse userInfoResponse = userClientService.fetchUser();
        College userCollege = userInfoResponse.getCollege();
        if (userCollege == null) {
            return List.of();
        }

        List<Map<Integer, List<LectureRoomResponseDto>>> statusOfRooms = new ArrayList<>(); //배열 인덱스마다 층고를 뜻함

        for (int floor = 1; floor <= 5; floor++) { //1번 인덱스 = 1층 강의실
            Map<Integer, List<LectureRoomResponseDto>> floorMap = new HashMap<>(); // 강의실 호수 : 해당 호수에서 진행되는 강의 스케줄
            for (LectureRoom lr : lectureRooms){
                int room = lr.getRoomNum();
                if ((room / 100) == floor) {
                    List<LectureRoomResponseDto> emptyRooms = lr.getSchedules().stream()
                                    .filter(schedule -> schedule.getDayOfWeek().equals(today))
                                    .filter(schedule -> schedule.getLecture() != null)
                                    .filter(schedule -> schedule.getLecture().getCollege() != null)
                                    .filter(schedule -> userCollege.equals(schedule.getLecture().getCollege()))
                                    .map(schedule -> LectureRoomResponseDto.of(schedule.getLecture(), room, schedule))
                                    .toList();
                    floorMap.put(room, emptyRooms);
                }
            }
            statusOfRooms.add(floorMap);
        }
        return statusOfRooms;
    }

    /**
     * @param building 건물 호관 번호
     * @param floor 층수 (전체 층은 0으로 입력)
     */
    public List<Map<Integer, List<LectureRoomResponseDto>>> statusOfDetailEmptyRoom(
            int building, int floor) {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        List<LectureRoom> lectureRooms = lectureRoomRepository.findAllWithSchedulesAndLectures();

        UserInfoResponse userInfoResponse = userClientService.fetchUser();
        College userCollege = userInfoResponse.getCollege();
        if (userCollege == null) return List.of();

        int startFloor = 0;
        int endFloor = 0;

        // 0이면 전체 층, 1~5 사이면 해당 층, 그 외는 예외 처리
        if (floor == 0) {
            startFloor = 1;
            endFloor = 5;
        } else if (floor >= 1 && floor <= 5) {
            startFloor = floor;
            endFloor = floor;
        } else {
            throw new IllegalArgumentException("유효하지 않은 층수입니다. 0 또는 1~5 사이의 값으로 입력해주세요.");
        }
        List<Map<Integer, List<LectureRoomResponseDto>>> statusOfRooms = new ArrayList<>(); //배열 인덱스마다 층고를 뜻함

        for (int floorIndex = startFloor; floorIndex <= endFloor; floorIndex++) { //1번 인덱스 = 1층 강의실
            Map<Integer, List<LectureRoomResponseDto>> floorMap = new HashMap<>(); // 강의실 호수 : 해당 호수에서 진행되는 강의 스케줄

            for (LectureRoom lr : lectureRooms){
                int room = lr.getRoomNum();
                if ((room / 100) != floorIndex) continue;

                List<LectureRoomResponseDto> emptyRooms = lr.getSchedules().stream()
                        .filter(schedule -> schedule.getDayOfWeek().equals(today))
                        .filter(schedule -> schedule.getLecture() != null)
                        .filter(schedule -> schedule.getLecture().getCollege() != null)
                        .filter(schedule -> schedule.getRoom() != null)
                        .filter(schedule -> schedule.getRoom().getBuildingNum() != null)
                        .filter(schedule -> userCollege.equals(schedule.getLecture().getCollege()))
                        .filter(schedule -> schedule.getRoom().getBuildingNum() == building)
                        .map(schedule -> LectureRoomResponseDto.of(schedule.getLecture(), room, schedule))
                        .toList();
                floorMap.put(room, emptyRooms);
            }
            statusOfRooms.add(floorMap);
        }
        return statusOfRooms;
    }
}
