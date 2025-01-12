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
        List<Professor> professors = infoRepository.findAllProfessorsByDepartment(department);
        log.info("[getProfessorByDepartment] '{}' 학과의 교수님 정보 모두 반환", department.getDescription());
        return professors.stream().map(ProfessorListResponseDto::of).toList();
    }

    public ProfessorThumbnailResponseDto getProfessorThumbnail(String id) {
        Professor professor = infoRepository.findProfessorById(new ObjectId(id))
                .orElseThrow(() -> new ProfessorNotFoundException("교수 정보를 찾을 수 없습니다."));
        log.info("[getProfessorThumbnail] {} 교수님의 정보 열람", professor.get_id().toString());
        return ProfessorThumbnailResponseDto.of(professor);
    }

    public void createProfessor(ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto) {
        if (infoRepository.findProfessorByEmail(professorCreateUpdateRequestDto.getEmail()).isPresent()){
            throw new ProfessorDuplicatedException("이미 존재하는 Professor 정보 입니다.");
        }
        Professor professor = Professor.of(professorCreateUpdateRequestDto);
        infoRepository.save(professor);
        log.info("[createProfessor] {} 교수님의 정보 생성", professor.getName());
    }

    public void updateProfessor(String id, ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto) {
        Professor professor = infoRepository.findProfessorById(new ObjectId(id))
                .orElseThrow(() -> new ProfessorNotFoundException("교수 정보를 찾을 수 없습니다."));
        professor.update(professorCreateUpdateRequestDto);
        infoRepository.save(professor);
        log.info("[updateProfessor] {} 교수님의 정보 업데이트", professor.get_id().toString());
    }



    public void deleteProfessor(String id) {
        Professor professor = infoRepository.findProfessorById(new ObjectId(id))
                .orElseThrow(() -> new ProfessorNotFoundException("교수 정보를 찾을 수 없습니다."));
        professor.delete();
        infoRepository.save(professor);
        log.info("[deleteProfessor] {} 교수님의 정보 삭제", professor.get_id().toString());
    }
}
