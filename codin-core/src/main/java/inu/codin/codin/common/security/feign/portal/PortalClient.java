package inu.codin.codin.common.security.feign.portal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "portal", url = "${feign.client.config.portal.url}")
public interface PortalClient {

    @PostMapping(
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    String signUp(
            @RequestParam("_enpass_login_") String enpassLogin,
            @RequestParam("username") String username,
            @RequestParam("password") String password
    );

}
