package inu.codin.codinticketingapi.domain.ticketing.service;

import inu.codin.codinticketingapi.common.security.util.SecurityUtil;
import inu.codin.codinticketingapi.domain.ticketing.dto.request.TicketingUserProfileRequest;
import inu.codin.codinticketingapi.domain.ticketing.dto.response.UserTicketingProfileResponse;
import inu.codin.codinticketingapi.domain.ticketing.entity.TicketingProfile;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingErrorCode;
import inu.codin.codinticketingapi.domain.ticketing.exception.TicketingException;
import inu.codin.codinticketingapi.domain.ticketing.repository.ProfileRepository;
import inu.codin.codinticketingapi.domain.user.dto.UserReply;
import inu.codin.codinticketingapi.domain.user.service.UserClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserClientService userClientService;

    /**
     * 유저의 티켓팅 수령자 정보 반환
     * @return UserTicketingProfileResponse
     */
    public UserTicketingProfileResponse getUserTicketingProfile() {
        String userId = userClientService.fetchUserIdAndUsername(SecurityUtil.getEmail()).userId();
        return UserTicketingProfileResponse.of(
                profileRepository.findByUserId(userId)
                    .orElseThrow(() -> new TicketingException(TicketingErrorCode.PROFILE_NOT_FOUND)));
    }

    /**
     * 유저의 티켓팅 수령자 정보 수정 및 생성
     * - 기존에 수령 정보가 존재하면 수정합니다
     */
    @Transactional
    public void createUserTicketingProfile(TicketingUserProfileRequest requestDto) {
        UserReply userReply = userClientService.fetchUserIdAndUsername(SecurityUtil.getEmail());

        TicketingProfile profile = profileRepository.findByUserId(userReply.userId())
                .map(existingProfile -> {
                    existingProfile.updateProfile(requestDto.getDepartment(), requestDto.getStudentId());
                    return existingProfile;
                })
                .orElseGet(() -> profileRepository.save(TicketingUserProfileRequest.toEntity(requestDto, userReply.userId(), userReply.username())));
    }
}
