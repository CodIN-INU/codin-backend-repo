package inu.codin.codin.domain.info.domain.professor.service;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.domain.professor.dto.response.ProfessorListResponseDto;
import inu.codin.codin.domain.info.domain.professor.dto.response.ProfessorThumbnailResponseDto;
import inu.codin.codin.domain.info.domain.professor.dto.request.ProfessorCreateUpdateRequestDto;
import inu.codin.codin.domain.info.domain.professor.entity.Professor;
import inu.codin.codin.domain.info.domain.professor.exception.ProfessorDuplicatedException;
import inu.codin.codin.domain.info.domain.professor.exception.ProfessorNotFoundException;
import inu.codin.codin.domain.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessorService {

    private final InfoRepository infoRepository;

    public List<ProfessorListResponseDto> getProfessorByDepartment(Department department){
        log.info("[getProfessorByDepartment] '{}' 학과의 교수님 정보 조회 시작", department.getDescription());
        List<Professor> professors = infoRepository.findAllProfessorsByDepartment(department);
        log.info("[getProfessorByDepartment] '{}' 학과의 교수님 정보 모두 반환", department.getDescription());
        return professors.stream().map(ProfessorListResponseDto::of).toList();
    }

    public ProfessorThumbnailResponseDto getProfessorThumbnail(String id) {
        log.info("[getProfessorThumbnail] 교수 ID '{}'로 정보 조회 시도", id);
        Professor professor = infoRepository.findProfessorById(new ObjectId(id))
                .orElseThrow(() -> new ProfessorNotFoundException("교수 정보를 찾을 수 없습니다."));
        log.info("[getProfessorThumbnail] {} 교수님의 정보 열람", professor.get_id().toString());
        return ProfessorThumbnailResponseDto.of(professor);
    }

    public void createProfessor(ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto) {
        log.info("[createProfessor] 교수 이메일 '{}'로 정보 생성 시도", professorCreateUpdateRequestDto.getEmail());
        if (infoRepository.findProfessorByEmail(professorCreateUpdateRequestDto.getEmail()).isPresent()){
            log.warn("[createProfessor] 교수 이메일 '{}' 이미 존재", professorCreateUpdateRequestDto.getEmail());
            throw new ProfessorDuplicatedException("이미 존재하는 Professor 정보 입니다.");
        }
        Professor professor = Professor.of(professorCreateUpdateRequestDto);
        infoRepository.save(professor);
        log.info("[createProfessor] {} 교수님의 정보 생성", professor.getName());
    }

    public void updateProfessor(String id, ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto) {
        Professor professor = infoRepository.findProfessorById(new ObjectId(id))
                .orElseThrow(() -> {
                    log.warn("[updateProfessor] 교수 ID '{}' 정보가 존재하지 않음", id);
                    return new ProfessorNotFoundException("교수 정보를 찾을 수 없습니다.");
                });
        professor.update(professorCreateUpdateRequestDto);
        infoRepository.save(professor);
        log.info("[updateProfessor] {} 교수님의 정보 업데이트", professor.get_id().toString());
    }



    public void deleteProfessor(String id) {
        log.info("[deleteProfessor] 교수 ID '{}'로 정보 삭제 시도", id);
        Professor professor = infoRepository.findProfessorById(new ObjectId(id))
                .orElseThrow(() -> {
                    log.warn("[deleteProfessor] 교수 ID '{}' 정보가 존재하지 않음", id);
                    return new ProfessorNotFoundException("교수 정보를 찾을 수 없습니다.");
                });professor.delete();
        infoRepository.save(professor);
        log.info("[deleteProfessor] {} 교수님의 정보 삭제", professor.get_id().toString());
    }
}
