package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.domain.admin.entity.Event;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.ParticipationCreateResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.Participation;
import inu.codin.codinticketingapi.domain.ticketing.entity.Profile;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.EventRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ParticipationRepository;
import inu.codin.codinticketingapi.domain.ticketing.repository.ProfileRepository;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import inu.codin.codinticketingapi.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TicketingService {

    private final EventRepository eventRepository;
    private final ProfileRepository profileRepository;
    private final ParticipationRepository participationRepository;

    private final UserClientService userClientService;

    public ParticipationCreateResponse createUserParticipation(Long eventId) {
        String userId = userClientService.fetchUserIdAndUsername(SecurityUtil.getEmail()).userId();

        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.PROFILE_NOT_FOUND));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new TicketingException(TicketingErrorCode.EVENT_NOT_FOUND));

        // int ticketNumber = Stock에서 티켓팅 번호 가져오기
        Participation participation = Participation.builder()
                .event(event)
                .ticketNumber(1)
                .profile(profile)
                .build();
        return ParticipationCreateResponse.of(participationRepository.save(participation));
    }

    // todo: 1. 특정 이벤트의 잔여수량 실시간 체크 기능 (STOMP?)
    // todo: 2.
}
