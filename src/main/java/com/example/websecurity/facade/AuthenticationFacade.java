package com.example.websecurity.facade;

import com.example.websecurity.api.dto.AuthenticationRequest;
import com.example.websecurity.api.dto.AuthenticationResponse;
import com.example.websecurity.persistence.User;
import com.example.websecurity.security.JwtService;
import com.example.websecurity.service.UserService;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor

public class AuthenticationFacade {

    private static final Logger log = LogManager.getLogger(AuthenticationFacade.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Transactional
    public AuthenticationResponse authenticate(@NotNull AuthenticationRequest request) {
        /*
        ispravljeno, ne smemo logovati senzitivan info, ovo je lako pogresiti zbog lomboka jer se ne vide getteri za password a @Data automatski generise toString sa passwordom!
        log.info("Authentication Facade: Authenticating user with request: {}", request);
        */
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userService.getUserByEmail(request.getEmail());
        String accessToken = jwtService.generateAccessToken(user);
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
