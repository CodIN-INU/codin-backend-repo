package inu.codin.codin.domain.info.domain.professor.service;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.domain.professor.dto.ProfessorCreateUpdateRequestDto;
import inu.codin.codin.domain.info.domain.professor.dto.ProfessorListResponseDto;
import inu.codin.codin.domain.info.domain.professor.dto.ProfessorThumbnailResponseDto;
import inu.codin.codin.domain.info.domain.professor.entity.Professor;
import inu.codin.codin.domain.info.domain.professor.exception.ProfessorDuplicatedException;
import inu.codin.codin.domain.info.domain.professor.exception.ProfessorNotFoundException;
import inu.codin.codin.domain.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final InfoRepository infoRepository;

    public List<ProfessorListResponseDto> getProfessorByDepartment(Department department){
        List<Professor> professors = infoRepository.findAllProfessorsByDepartment(department);
        return professors.stream().map(ProfessorListResponseDto::of).toList();
    }

    public ProfessorThumbnailResponseDto getProfessorThumbnail(String id) {
        Professor professor = infoRepository.findProfessorById(id)
                .orElseThrow(() -> new ProfessorNotFoundException("교수 정보를 찾을 수 없습니다."));
        return ProfessorThumbnailResponseDto.of(professor);
    }

    public void createProfessor(ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto) {
        if (infoRepository.findProfessorByEmail(professorCreateUpdateRequestDto.getEmail()).isPresent()){
            throw new ProfessorDuplicatedException("이미 존재하는 Professor 정보 입니다.");
        }
        Professor professor = Professor.of(professorCreateUpdateRequestDto);
        infoRepository.save(professor);
    }

    public void updateProfessor(String id, ProfessorCreateUpdateRequestDto professorCreateUpdateRequestDto) {
        Professor professor = infoRepository.findProfessorById(id)
                .orElseThrow(() -> new ProfessorNotFoundException("교수 정보를 찾을 수 없습니다."));
        professor.update(professorCreateUpdateRequestDto);
        infoRepository.save(professor);
    }



    public void deleteProfessor(String id) {
        Professor professor = infoRepository.findProfessorById(id)
                .orElseThrow(() -> new ProfessorNotFoundException("교수 정보를 찾을 수 없습니다."));
        professor.delete();
        infoRepository.save(professor);
    }
}
