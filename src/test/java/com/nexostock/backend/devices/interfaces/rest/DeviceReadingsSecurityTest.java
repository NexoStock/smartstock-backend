package com.nexostock.backend.devices.interfaces.rest;

import com.nexostock.backend.devices.application.commandservices.SensorCommandService;
import com.nexostock.backend.devices.domain.model.aggregates.SensorReading;
import com.nexostock.backend.devices.domain.model.commands.ReceiveReadingCommand;
import com.nexostock.backend.iam.application.internal.outboundservices.tokens.TokenService;
import com.nexostock.backend.shared.application.result.ApplicationError;
import com.nexostock.backend.shared.application.result.Result;
import com.nexostock.backend.shared.infrastructure.configuration.JwtSecurityConfiguration;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeviceReadingsController.class)
@ContextConfiguration(classes = {
        DeviceReadingsController.class,
        JwtSecurityConfiguration.class,
        DeviceReadingsSecurityTest.TestCorsConfiguration.class
})
class DeviceReadingsSecurityTest {

    private static final String URL = "/api/device-readings/SENSOR-001";

    private static final String BODY =
            "{\"pesoKg\":2.500,\"medidoEn\":\"2026-10-01T12:00:00Z\"}";

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private TokenService tokens;

    @MockitoBean
    private SensorCommandService commands;

    @MockitoBean
    private ProblemDetailFromApplicationErrorAssembler problems;

    @BeforeEach
    void configureDeviceResponses() {
        when(commands.handle(any(ReceiveReadingCommand.class)))
                .thenAnswer(invocation -> {
                    ReceiveReadingCommand command =
                            invocation.getArgument(0);

                    if ("valid-device-key".equals(command.deviceKey())) {
                        return Result.success(new SensorReading(
                                1L,
                                new BigDecimal("2.500"),
                                Instant.parse("2026-10-01T12:00:00Z")
                        ));
                    }

                    return Result.failure(
                            ApplicationError.unauthorized(
                                    "INVALID_DEVICE_CREDENTIALS"
                            )
                    );
                });

        when(problems.toResponseEntity(any(ApplicationError.class)))
                .thenReturn(ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(ProblemDetail.forStatus(
                                HttpStatus.UNAUTHORIZED
                        )));
    }

    @Test
    void validDeviceKeyWithoutJwtReturns201() throws Exception {
        mvc.perform(post(URL)
                        .header("X-Device-Key", "valid-device-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated());
    }

    @Test
    void invalidDeviceKeyWithoutJwtReturns401() throws Exception {
        mvc.perform(post(URL)
                        .header("X-Device-Key", "wrong-device-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingDeviceKeyReturns401() throws Exception {
        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithoutJwtReturns401() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestCorsConfiguration {

        @Bean("corsConfigurationSource")
        CorsConfigurationSource corsConfigurationSource() {
            return new UrlBasedCorsConfigurationSource();
        }
    }
}