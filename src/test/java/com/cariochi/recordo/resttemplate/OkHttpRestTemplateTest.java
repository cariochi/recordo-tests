package com.cariochi.recordo.resttemplate;

import com.cariochi.recordo.RecordoTestsApplication;
import com.cariochi.recordo.annotation.EnableHttpMocks;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
        classes = {RecordoTestsApplication.class, OkHttpRestTemplateTest.Config.class},
        properties = "rest-template.okhttp.enabled=true"
)
public class OkHttpRestTemplateTest extends RestTemplateTest {

    @Autowired
    @EnableHttpMocks
    private OkHttpClient client;

    @Configuration
    @ConditionalOnProperty("rest-template.okhttp.enabled")
    public static class Config {

        @Bean
        public OkHttpClient client() {
            return new OkHttpClient();
        }

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate(new OkHttp3ClientHttpRequestFactory(client()));
        }
    }

}
