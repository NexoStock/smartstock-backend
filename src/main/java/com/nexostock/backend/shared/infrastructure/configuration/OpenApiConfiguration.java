
package com.nexostock.backend.shared.infrastructure.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Value("${documentation.application.name}")
    private String applicationName;

    @Value("${documentation.application.description}")
    private String applicationDescription;

    @Value("${documentation.application.version}")
    private String applicationVersion;

    @Value("${documentation.server.production-url}")
    private String productionUrl;

    @Bean
    public OpenAPI nexostockOpenApi() {
        final String scheme = "bearerAuth";

        return new OpenAPI()
                .info(
                        new Info()
                                .title(applicationName)
                                .description(applicationDescription)
                                .version(applicationVersion)
                                .contact(
                                        new Contact()
                                                .name("NexoStock Team")
                                                .url("https://github.com/NexoStock")
                                )
                                .license(
                                        new License()
                                                .name("MIT License")
                                                .url("https://opensource.org/licenses/MIT")
                                )
                )
                .externalDocs(
                        new ExternalDocumentation()
                                .description("NexoStock project report")
                                .url("https://github.com/NexoStock/smartstock-docs")
                )
                .servers(
                        List.of(
                                new Server()
                                        .url("http://localhost:8080")
                                        .description("Local"),
                                new Server()
                                        .url(productionUrl)
                                        .description("Production")
                        )
                )
                .addSecurityItem(
                        new SecurityRequirement().addList(scheme)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        scheme,
                                        new SecurityScheme()
                                                .name(scheme)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}
