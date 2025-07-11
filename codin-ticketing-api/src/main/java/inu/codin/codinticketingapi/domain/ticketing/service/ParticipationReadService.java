package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.ticketing.dto.response.EventParticipationHistoryDto;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParticipationReadService {

    private final ParticipationRepository participationRepository;

    public Page<EventParticipationHistoryDto> getUserEventHistory(String userId, Pageable pageable) {
        return participationRepository.findHistoryByUserId(userId, pageable);
    }

    public Page<EventParticipationHistoryDto> getUserEventHistoryByCanceled(String userId, Pageable pageable, boolean canceled) {
        return participationRepository.findHistoryByUserIdAndCanceled(userId, canceled, pageable);
    }
}