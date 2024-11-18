package inu.codin.codin.domain.info.domain.professor.service;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.domain.professor.dto.ProfessorListResDTO;
import inu.codin.codin.domain.info.domain.professor.dto.ProfessorThumbnailResDTO;
import inu.codin.codin.domain.info.domain.professor.entity.Professor;
import inu.codin.codin.domain.info.domain.professor.exception.ProfessorNotFoundException;
import inu.codin.codin.domain.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final InfoRepository infoRepository;

    public List<ProfessorListResDTO> getProfessorByDepartment(Department department){
        List<Professor> professors = infoRepository.findAllProfessorsByDepartment(department);
        return professors.stream().map(ProfessorListResDTO::of).toList();
    }

    public ProfessorThumbnailResDTO getProfessorThumbnail(String id) {
        Professor professor = infoRepository.findProfessorById(id)
                .orElseThrow(() -> new ProfessorNotFoundException("교수 정보를 찾을 수 없습니다."));
        return ProfessorThumbnailResDTO.of(professor);
    }
}
