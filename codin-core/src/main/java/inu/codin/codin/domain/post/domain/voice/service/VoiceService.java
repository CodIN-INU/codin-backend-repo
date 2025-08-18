package inu.codin.codin.domain.post.domain.voice.service;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.post.domain.voice.dto.VoiceBoxCreateRequest;
import inu.codin.codin.domain.post.domain.voice.dto.VoiceBoxDetailResponse;
import inu.codin.codin.domain.post.domain.voice.dto.VoiceBoxPageResponse;
import inu.codin.codin.domain.post.domain.voice.entity.VoiceEntity;
import inu.codin.codin.domain.post.domain.voice.repository.VoiceRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoiceService {

    private final VoiceRepository voiceRepository;
    private static final int PAGE_SIZE = 20;

    public VoiceBoxDetailResponse createVoiceBox(VoiceBoxCreateRequest voiceBoxCreateRequest) {
        VoiceEntity voiceEntity = VoiceEntity.builder()
                .question(voiceBoxCreateRequest.getQuestion())
                .department(voiceBoxCreateRequest.getDepartment())
                .build();

        return VoiceBoxDetailResponse.of(voiceRepository.save(voiceEntity));
    }

    public VoiceBoxPageResponse getVoiceBoxListByDepartment(Department department, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("createdAt").descending());
        Page<VoiceEntity> voiceEntities = voiceRepository.findAnsweredByDepartmentAndNotDeleted(department, pageable);

        return getVoiceBoxPageResponse(pageNumber, voiceEntities);
    }

    public void toggleVoiceBox(String boxId, Boolean positive) {
        if (!ObjectIdUtil.isValid(boxId)) {
            throw new IllegalArgumentException("유효하지 않은 ID 형식입니다.");
        }

        ObjectId objectId = ObjectIdUtil.toObjectId(boxId);
        VoiceEntity voiceEntity = voiceRepository.findByIdAndNotDeleted(objectId)
                .orElseThrow(() -> new IllegalArgumentException("익명의 소리함 질문을 찾을 수 없습니다."));

        ObjectId currentUserId = SecurityUtils.getCurrentUserId();

        if (positive) {
            voiceEntity.votePositiveToggle(currentUserId);
        } else {
            voiceEntity.voteOppositeToggle(currentUserId);
        }

        voiceRepository.save(voiceEntity);
    }

    public VoiceBoxPageResponse getAllNotAnsweredList(Department department, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by("createdAt").descending());
        Page<VoiceEntity> notAnsweredVoices = voiceRepository.findByDepartmentAndAnswerIsNullAndNotDeleted(department, pageable);

        return getVoiceBoxPageResponse(pageNumber, notAnsweredVoices);
    }

    public VoiceBoxDetailResponse addAnswer(String boxId, String answer) {
        if (!ObjectIdUtil.isValid(boxId)) {
            throw new IllegalArgumentException("유효하지 않은 ID 형식입니다.");
        }

        ObjectId objectId = ObjectIdUtil.toObjectId(boxId);
        VoiceEntity voiceEntity = voiceRepository.findByIdAndNotDeleted(objectId)
                .orElseThrow(() -> new IllegalArgumentException("익명의 소리함 질문을 찾을 수 없습니다."));

        voiceEntity.updateAnswer(answer);

        VoiceEntity savedEntity = voiceRepository.save(voiceEntity);
        return VoiceBoxDetailResponse.of(savedEntity);
    }

    public void deleteVoiceBox(String boxId) {
        if (!ObjectIdUtil.isValid(boxId)) {
            throw new IllegalArgumentException("유효하지 않은 ID 형식입니다.");
        }

        ObjectId objectId = ObjectIdUtil.toObjectId(boxId);
        VoiceEntity voiceEntity = voiceRepository.findByIdAndNotDeleted(objectId)
                .orElseThrow(() -> new IllegalArgumentException("익명의 소리함 질문을 찾을 수 없습니다."));

        voiceEntity.delete();
        voiceRepository.save(voiceEntity);
    }

    private static VoiceBoxPageResponse getVoiceBoxPageResponse(int pageNumber, Page<VoiceEntity> voiceEntityPage) {
        List<VoiceBoxDetailResponse> responses = voiceEntityPage.stream()
                .map(VoiceBoxDetailResponse::of)
                .collect(Collectors.toList());

        long totalElements = voiceEntityPage.getTotalElements();
        long nextPage = voiceEntityPage.hasNext() ? pageNumber + 1 : -1;

        return VoiceBoxPageResponse.of(responses, totalElements, nextPage);
    }
}

