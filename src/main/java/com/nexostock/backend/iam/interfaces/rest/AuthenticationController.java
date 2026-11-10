package com.nexostock.backend.iam.interfaces.rest;

import com.nexostock.backend.iam.application.commandservices.AccountCommandService;
import com.nexostock.backend.iam.application.commandservices.AccountCommandService.SignedIn;
import com.nexostock.backend.iam.domain.model.commands.RegisterBusinessCommand;
import com.nexostock.backend.iam.domain.model.commands.SignInCommand;
import com.nexostock.backend.iam.domain.model.valueobjects.EmailAddress;
import com.nexostock.backend.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Register and sign in")
public class AuthenticationController {

    public record RegisterResource(
            @NotBlank @Size(max = 255) @Schema(example = "Bodega Don Pepe") String businessName,
            @Size(max = 255) @Schema(example = "bodega") String businessType,
            @Size(max = 32) @Schema(example = "+51999999999", description = "Optional WhatsApp number for alerts") String phone,
            @NotBlank @Email @Schema(example = "pepe@bodega.pe") String email,
            @NotBlank @Size(min = 8, max = 72) @Schema(example = "Clave-Segura-123") String password) {}

    public record LoginResource(
            @NotBlank @Email @Schema(example = "pepe@bodega.pe") String email,
            @NotBlank @Schema(example = "Clave-Segura-123") String password) {}

    public record SessionResource(String token, String email, String businessName, String businessType) {}

    private final AccountCommandService commands;
    private final ProblemDetailFromApplicationErrorAssembler problems;

    public AuthenticationController(
            AccountCommandService commands, ProblemDetailFromApplicationErrorAssembler problems) {
        this.commands = commands;
        this.problems = problems;
    }

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register a business", description = "Creates the business and its owner and returns a JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Business registered"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<?> register(@Valid @RequestBody RegisterResource body) {
        var command =
                new RegisterBusinessCommand(
                        body.businessName(), body.businessType(), body.phone(), new EmailAddress(body.email()), body.password());
        return commands
                .handle(command)
                .<ResponseEntity<?>>fold(
                        s -> ResponseEntity.status(HttpStatus.CREATED).body(toResource(s)),
                        problems::toResponseEntity);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Sign in", description = "Returns an 8-hour JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or data")
    })
    public ResponseEntity<?> login(@Valid @RequestBody LoginResource body) {
        var command = new SignInCommand(new EmailAddress(body.email()), body.password());
        return commands
                .handle(command)
                .<ResponseEntity<?>>fold(s -> ResponseEntity.ok(toResource(s)), problems::toResponseEntity);
    }

    private SessionResource toResource(SignedIn s) {
        return new SessionResource(s.token(), s.account().getEmail().value(), s.businessName(), s.businessType());
    }
}
