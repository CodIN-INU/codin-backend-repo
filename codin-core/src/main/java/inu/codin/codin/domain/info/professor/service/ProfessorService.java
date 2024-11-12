package inu.codin.codin.domain.info.professor.service;

import inu.codin.codin.common.Department;
import inu.codin.codin.domain.info.professor.dto.ProfessorListResDTO;
import inu.codin.codin.domain.info.professor.dto.ProfessorThumbnailResDTO;
import inu.codin.codin.domain.info.professor.entity.Professor;
import inu.codin.codin.domain.info.professor.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;

    public List<ProfessorListResDTO> getProfessorByDepartment(Department department){
        List<Professor> professors = professorRepository.findAllByDepartment(department);
        return professors.stream().map(ProfessorListResDTO::of).toList();
    }

    public ProfessorThumbnailResDTO getProfessorThumbnail(String id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("not found"));
        return ProfessorThumbnailResDTO.of(professor);
    }
}
