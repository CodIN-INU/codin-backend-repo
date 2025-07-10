package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.common.security.util.SecurityUtil;
import inu.codin.codinticketingapi.domain.ticketing.dto.request.TicketingUserProfileRequest;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.UserTicketingProfileResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingProfile;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;

    public UserTicketingProfileResponse getUserTicketingProfile() {
        String username = SecurityUtil.getUsername();
        // todo: username을 통해서 메인 API 서버에서 userId 추출
        String userId = null;

        return UserTicketingProfileResponse.of(
                profileRepository.findByUserId(userId)
                    .orElseThrow(() -> new TicketingException(TicketingErrorCode.PROFILE_NOT_FOUND)));
    }

    @Transactional
    public void createUserTicketingProfile(TicketingUserProfileRequest requestDto) {
        String username = SecurityUtil.getUsername();
        // todo: username을 통해서 메인 API 서버에서 userId 추출
        String userId = null;
        profileRepository.save(TicketingProfile.builder()
                .userId(userId)
                .department(requestDto.getDepartment())
                .studentId(requestDto.getStudentId())
                .build());
    }
}
