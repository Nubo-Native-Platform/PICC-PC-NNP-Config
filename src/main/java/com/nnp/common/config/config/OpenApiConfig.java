package com.nnp.common.config.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI 3.0 / Swagger UI Configuration for PICC-PC-NNP-Config.
 */
@Configuration
public class OpenApiConfig {

    private final String serverPort;

    public OpenApiConfig(@Value("${server.port:8888}") String serverPort) {
        this.serverPort = serverPort;
    }

    @Bean
    public OpenAPI nnpConfigOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PICC-PC-NNP-Config REST API")
                        .description("Central Configuration Service for Nubo Native Platform (NNP) "
                                + "supporting plaintext and post-quantum hybrid encrypted environment values "
                                + "(X25519 + ML-KEM-768 with AES-256-GCM).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Nubo Native Platform Team")
                                .email("contribution@nubons.com")
                                .url("https://github.com/Nubo-Native-Platform/PICC-PC-NNP-Config"))
                        .license(new License()
                                .name("Apache License 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("/").description("Current Server Context"),
                        new Server().url("http://localhost:" + serverPort).description("Local Development Server")
                ));
    }
}
