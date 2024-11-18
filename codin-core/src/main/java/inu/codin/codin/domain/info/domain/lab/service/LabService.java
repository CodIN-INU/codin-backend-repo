package inu.codin.codin.domain.info.domain.lab.service;

import inu.codin.codin.domain.info.domain.lab.dto.LabListResponseDto;
import inu.codin.codin.domain.info.domain.lab.dto.LabThumbnailResponseDto;
import inu.codin.codin.domain.info.domain.lab.entity.Lab;
import inu.codin.codin.domain.info.domain.lab.exception.LabNotFoundException;
import inu.codin.codin.domain.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LabService {

    private final InfoRepository infoRepository;

    public LabThumbnailResponseDto getLabThumbnail(String id) {
        Lab lab = infoRepository.findLabById(id)
                .orElseThrow(() -> new LabNotFoundException("연구실 정보를 찾을 수 없습니다."));
        return LabThumbnailResponseDto.of(lab);
    }

    public List<LabListResponseDto> getAllLab() {
        List<Lab> labs = infoRepository.findAllLabs();
        return labs.stream().map(LabListResponseDto::of).toList();
    }
}
