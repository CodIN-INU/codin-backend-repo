package inu.codin.codinticketingapi.domain.user.service;

import inu.codin.codinticketingapi.domain.user.dto.UserApiResponse;
import inu.codin.codinticketingapi.domain.user.dto.UserInfoResponse;
import inu.codin.codinticketingapi.domain.user.fegin.UserFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserClientService {

    private final UserFeignClient userFeignClient;

    public UserInfoResponse fetchUser() {
        UserApiResponse userApiResponse = userFeignClient.getUser();
        log.info("[fetchUser] Fetching User Info By User Token, userApiResponse={}", userApiResponse.toString());
        return userApiResponse.getData();
    }
}
