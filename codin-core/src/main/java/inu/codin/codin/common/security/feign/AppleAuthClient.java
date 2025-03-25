package inu.codin.codin.common.security.feign;

import inu.codin.codin.common.security.dto.apple.key.ApplePublicKeyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "appleAuthClient", url = "${apple.auth.public-key-url}")
public interface AppleAuthClient {
    @GetMapping
    ApplePublicKeyResponse getAppleAuthPublicKey();
}
