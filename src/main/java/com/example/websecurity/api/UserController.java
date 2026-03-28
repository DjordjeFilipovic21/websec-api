package com.example.websecurity.api;

import com.example.websecurity.api.dto.AuthenticationRequest;
import com.example.websecurity.api.dto.AuthenticationResponse;
import com.example.websecurity.facade.AuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LogManager.getLogger(UserController.class);

    private final AuthenticationFacade authenticationFacade;

    @Operation(summary = "Authenticate User", description = "Authenticate user")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(
            @RequestBody AuthenticationRequest request
    ) {
        /*
        ispravljeno, ne smemo logovati senzitivan info, ovo je lako pogresiti zbog lomboka jer se ne vide getteri za password a @Data automatski generise toString sa passwordom!
        log.info("User Controller: Received login request: {}", request);
        */
        System.out.println(org.apache.logging.log4j.LogManager.getLogger().getClass());
        return ResponseEntity.ok(authenticationFacade.authenticate(request));
    }

}
