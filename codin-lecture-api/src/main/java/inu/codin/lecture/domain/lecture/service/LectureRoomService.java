package inu.codin.lecture.domain.lecture.service;

import inu.codin.common.entity.College;
import inu.codin.lecture.domain.lecture.dto.LectureRoomResponseDto;
import inu.codin.lecture.domain.lecture.entity.LectureRoom;
import inu.codin.lecture.domain.lecture.entity.LectureSchedule;
import inu.codin.lecture.domain.lecture.exception.LectureErrorCode;
import inu.codin.lecture.domain.lecture.exception.LectureException;
import inu.codin.lecture.domain.lecture.repository.LectureRoomRepository;
import inu.codin.lecture.domain.lecture.repository.LectureScheduleRepository;
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
    private final LectureScheduleRepository lectureScheduleRepository;
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
        if (floor != 0 && (floor < 1 || floor > 5)) {
            throw new LectureException(LectureErrorCode.FLOOR_NOT_VALID);
        }

        DayOfWeek today = LocalDateTime.now().getDayOfWeek();

        UserInfoResponse userInfoResponse = userClientService.fetchUser();
        College userCollege = userInfoResponse.getCollege();
        if (userCollege == null) return List.of();

        // 0이면 전체 층, 1~5 사이면 해당 층, 그 외는 예외 처리
        int startFloor = (floor == 0) ? 1 : floor;
        int endFloor = (floor == 0) ? 5 : floor;

        // 결과 틀 생성
        List<Map<Integer, List<LectureRoomResponseDto>>> statusOfRooms = new ArrayList<>();
        for (int f = startFloor; f <= endFloor; f++) {
            statusOfRooms.add(new HashMap<>());
        }

        // 강의실 목록 먼저 가져와서 빈 리스트로 key 생성
        List<LectureRoom> rooms = lectureRoomRepository.findRoomsByBuildingAndFloor(building, floor);
        for (LectureRoom r : rooms) {
            int roomNum = r.getRoomNum();
            int roomFloor = roomNum / 100;

            if (roomFloor < startFloor || roomFloor > endFloor) continue;

            int idx = roomFloor - startFloor;
            statusOfRooms.get(idx).put(roomNum, new ArrayList<>());
        }

        List<LectureSchedule> lectureSchedules = lectureScheduleRepository.findSchedulesForEmptyRoom(
                building, floor, today, userCollege);
        for (LectureSchedule ls : lectureSchedules) {
            int roomNum = ls.getRoom().getRoomNum();
            int roomFloor = roomNum / 100;

            if (roomFloor < startFloor || roomFloor > endFloor) continue;

            int idx = roomFloor - startFloor;
            Map<Integer, List<LectureRoomResponseDto>> floorMap = statusOfRooms.get(idx);

            floorMap.computeIfAbsent(roomNum, k -> new ArrayList<>())
                    .add(LectureRoomResponseDto.of(ls.getLecture(), roomNum, ls));
        }

        return statusOfRooms;
    }
}
