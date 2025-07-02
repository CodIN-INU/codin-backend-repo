package inu.codin.codin.domain.info.service;

import inu.codin.codin.domain.info.dto.request.LabCreateUpdateRequestDto;
import inu.codin.codin.domain.info.dto.response.LabListResponseDto;
import inu.codin.codin.domain.info.dto.response.LabThumbnailResponseDto;
import inu.codin.codin.domain.info.entity.Lab;
import inu.codin.codin.domain.info.exception.InfoErrorCode;
import inu.codin.codin.domain.info.exception.InfoException;
import inu.codin.codin.domain.info.repository.InfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LabService {

    private final InfoRepository infoRepository;

    public LabThumbnailResponseDto getLabThumbnail(String id) {
        Lab lab = infoRepository.findLabById(new ObjectId(id))
                .orElseThrow(() -> {
                    log.warn("[getLabThumbnail] 연구실 정보 조회 실패, 연구실 ID: {}", id);
                    return new InfoException(InfoErrorCode.LAB_NOT_FOUND);
                });
        log.info("[getLabThumbnail] {}의 연구실 정보 열람", id);
        return LabThumbnailResponseDto.of(lab);
    }

    public List<LabListResponseDto> getAllLab() {
        List<Lab> labs = infoRepository.findAllLabs();
        List<LabListResponseDto> list = labs.stream().map(LabListResponseDto::of).toList();
        log.info("[getAllLab] 모든 연구실 정보 반환");
        return list;
    }

    public void createLab(LabCreateUpdateRequestDto labCreateUpdateRequestDto) {
        Lab lab = Lab.of(labCreateUpdateRequestDto);
        infoRepository.save(lab);
        log.info("[createLab] '{}'의 연구실 정보 생성", lab.getTitle());
    }

    public void updateLab(LabCreateUpdateRequestDto labCreateUpdateRequestDto, String id) {
        Lab lab = infoRepository.findLabById(new ObjectId(id))
                .orElseThrow(() -> {
                    log.warn("[updateLab] 연구실 정보 업데이트 실패, 연구실 ID: {}", id);
                    return new InfoException(InfoErrorCode.LAB_NOT_FOUND);
                });
        lab.update(labCreateUpdateRequestDto);
        infoRepository.save(lab);
        log.info("[updateLab] {}의 연구실 정보 업데이트", lab.get_id().toString());
    }

    public void deleteLab(String id) {
        Lab lab = infoRepository.findLabById(new ObjectId(id))
                .orElseThrow(() -> new InfoException(InfoErrorCode.LAB_NOT_FOUND));
        lab.delete();
        infoRepository.save(lab);
        log.info("[deleteLab] {}의 연구실 정보 삭제", lab.get_id().toString());
    }
}
