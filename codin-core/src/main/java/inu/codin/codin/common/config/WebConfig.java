package inu.codin.codin.common.config;

import inu.codin.codin.common.util.MultipartJackson2HttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter;

    public WebConfig(MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter) {
        this.multipartJackson2HttpMessageConverter = multipartJackson2HttpMessageConverter;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 컨버터 리스트에 사용자 정의 컨버터 추가
        converters.add(multipartJackson2HttpMessageConverter);
    }
}