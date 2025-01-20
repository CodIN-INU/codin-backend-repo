package inu.codin.codin.domain.user.feign;

import inu.codin.codin.domain.user.feign.dto.UserPortalSignUpRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "portal", url = "${feign.client.config.portal.url}")
public interface PortalClient {

    @PostMapping
    String signUp(@RequestBody UserPortalSignUpRequestDto userSignUpRequestDto);
}
