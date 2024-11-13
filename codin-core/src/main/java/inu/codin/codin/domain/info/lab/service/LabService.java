package inu.codin.codin.domain.info.lab.service;

import inu.codin.codin.domain.info.lab.dto.LabListResDTO;
import inu.codin.codin.domain.info.lab.dto.LabThumbnailResDTO;
import inu.codin.codin.domain.info.lab.entity.Lab;
import inu.codin.codin.domain.info.lab.exception.LabNotFoundException;
import inu.codin.codin.domain.info.lab.repository.LabRepository;
import inu.codin.codin.domain.info.professor.entity.Professor;
import inu.codin.codin.domain.info.professor.repository.ProfessorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabService {

    private final LabRepository labRepository;
    private final ProfessorRepository professorRepository;

    public LabThumbnailResDTO getLabThumbnail(String id) {
        Lab lab = labRepository.findById(id)
                .orElseThrow(() -> new LabNotFoundException("연구실 정보를 찾을 수 없습니다."));
        return LabThumbnailResDTO.of(lab);
    }

    //잠시 교수님과 연구실을 참조하기 위해 만들어놓음. 추후 삭제 예정
    @Transactional
    public List<Professor> joinlab() {
        List<Professor> professors = professorRepository.findAll();
        List<Lab> labs = labRepository.findAll();

        for (Professor professor : professors) {
            for (Lab lab : labs) {
                if (professor.getName().equals(lab.getProfessor())) {
                    professor.updateLab(lab);
                    professorRepository.save(professor);
                    break;
                }
            }
        }
        return professors;
    }

    public List<LabListResDTO> getAllLab() {
        List<Lab> labs = labRepository.findAll();
        return labs.stream().map(LabListResDTO::of).toList();
    }
}
