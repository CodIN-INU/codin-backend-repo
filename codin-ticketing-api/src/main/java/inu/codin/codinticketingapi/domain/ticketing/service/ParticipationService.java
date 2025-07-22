package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto;
import inu.codin.codinticketingapi.domain.ticketing.entity.ParticipationStatus;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private final ParticipationRepository participationRepository;

    @Deprecated
    @Transactional(readOnly = true)
    public Page<EventParticipationHistoryDto> getUserEventHistory(String userId, Pageable pageable) {
        return participationRepository.findHistoryByUserId(userId, pageable);
    }

    @Deprecated
    @Transactional(readOnly = true)
    public Page<EventParticipationHistoryDto> getUserEventHistoryByCanceled(String userId, ParticipationStatus status, Pageable pageable) {
        return participationRepository.findHistoryByUserIdAndCanceled(userId, status, pageable);
    }
}