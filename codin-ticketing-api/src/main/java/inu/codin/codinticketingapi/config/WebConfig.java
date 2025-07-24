package inu.codin.codinticketingapi.config;

import inu.codin.codinticketingapi.common.util.MultipartJackson2HttpMessageConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter;

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 컨버터 리스트에 사용자 정의 컨버터 추가
        converters.add(multipartJackson2HttpMessageConverter);
    }
}
