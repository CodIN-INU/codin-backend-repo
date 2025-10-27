package inu.codin.codinticketingapi.domain.user.fegin;

import inu.codin.codinticketingapi.domain.user.dto.UserApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "userClient", url = "${user.client.url}")
public interface UserFeignClient {
    @GetMapping("/users")
    UserApiResponse getUser();
}
