package inu.codin.codin.common.security.feign.inu;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "inu", url = "${feign.client.config.inu.url}")
public interface InuClient {

    @GetMapping
    Map<String, String> status(@RequestHeader("Authorization") String basic);

}
