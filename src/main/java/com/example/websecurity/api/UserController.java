package com.example.websecurity.api;

import com.example.websecurity.api.dto.AuthenticationRequest;
import com.example.websecurity.api.dto.AuthenticationResponse;
import com.example.websecurity.facade.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final AuthenticationFacade authenticationFacade;

    @Operation(summary = "Authenticate User", description = "Authenticate user")
    @PostMapping("/login2")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ) {
        log.info("User Controller: Received login request: {}", request);
        return ResponseEntity.ok(authenticationFacade.authenticate(request));
    }

    @Operation(summary = "Authenticate User", description = "Authenticate user user enumeration vuln")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginVuln(
            @RequestBody AuthenticationRequest request
    ) {
        log.info("User Controller: Received login vuln request: {}", request);
        return ResponseEntity.ok(authenticationFacade.authenticateVuln(request));
    }

}
