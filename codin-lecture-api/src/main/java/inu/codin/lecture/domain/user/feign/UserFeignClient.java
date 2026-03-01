package inu.codin.lecture.domain.user.feign;

import inu.codin.lecture.domain.user.dto.UserApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "userClient", url = "${server.feign.url}")
public interface UserFeignClient {
    @GetMapping("/users")
    UserApiResponse getUser();
}
