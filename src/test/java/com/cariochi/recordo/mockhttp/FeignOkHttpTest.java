package com.cariochi.recordo.mockhttp;

import com.cariochi.recordo.EnableRecordo;
import com.cariochi.recordo.RecordoExtension;
import com.cariochi.recordo.RecordoTestsApplication;
import com.cariochi.recordo.given.Given;
import com.cariochi.recordo.mockhttp.dto.Gist;
import com.cariochi.recordo.mockhttp.dto.GistResponse;
import com.cariochi.recordo.verify.Expected;
import com.cariochi.recordo.verify.Verify;
import feign.Client;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@SpringBootTest(
        classes = {RecordoTestsApplication.class, FeignOkHttpTest.Config.class},
        properties = "feign.okhttp.enabled=true"
)
@ExtendWith(RecordoExtension.class)
class FeignOkHttpTest {

    @Autowired
    @EnableRecordo
    private OkHttpClient client;

    @Autowired
    protected GitHub gitHub;

    @Test
    @MockHttp("/mockhttp/feign-ok-http/should_retrieve_gists.rest.json")
    void should_retrieve_gists(
            @Verify("/mockhttp/gists.json") Expected<List<GistResponse>> expected
    ) {
        expected.assertEquals(gitHub.getGists());
    }

    @Test
    void should_create_gist(
            @Given("/mockhttp/gist.json") Gist gist,
            @MockHttp("/mockhttp/feign-ok-http/should_create_gist.http.json") MockHttpServer mockHttpServer,
            @Verify("/mockhttp/feign-ok-http/gist_response.json") Expected<GistResponse> expectedResponse,
            @Verify("/mockhttp/gist.json") Expected<Gist> expectedGist
    ) {
        try (final MockHttpContext context = mockHttpServer.run()) {

            context.set("gistId", "16d0b491b237960fd5bf3ba503a3d18b");

            GistResponse response = gitHub.createGist(gist);
            final GistResponse updateResponse = gitHub.updateGist(response.getId(), gist);
            final Gist createdGist = gitHub.getGist(response.getId(), "hello world");
            gitHub.deleteGist(response.getId());

            expectedResponse.assertEquals(updateResponse);
            expectedGist.assertEquals(createdGist);
        }
    }

    @Configuration
    @ConditionalOnProperty("feign.okhttp.enabled")
    @EnableFeignClients
    public static class Config {

        @Bean
        public OkHttpClient client() {
            return new OkHttpClient();
        }

        @Bean
        public Client feignClient() {
            return new feign.okhttp.OkHttpClient(client());
        }
    }

}