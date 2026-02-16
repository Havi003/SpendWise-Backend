package com.smartcoach.spendwise.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartcoach.spendwise.dto.request.LoginRequest;
import com.smartcoach.spendwise.dto.request.UserRegistrationRequest;
import com.smartcoach.spendwise.dto.response.JwtResponse;
import com.smartcoach.spendwise.dto.response.LoginResponse;
import com.smartcoach.spendwise.dto.response.UserResponse;
import com.smartcoach.spendwise.dto.response.WsResponse;
import com.smartcoach.spendwise.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.core.user.OAuth2User;



@RequiredArgsConstructor
@RequestMapping ("/api/auth")
@CrossOrigin (origins = "http://localhost:3000")
@RestController
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping ("/register")
    public Mono<WsResponse<UserResponse>> register(@Valid @RequestBody UserRegistrationRequest request){

        return authService.register(request);
        // .map(reponse -> ResponseEntity.status(HttpStatus.CREATED).body(reponse)
//  );
    }

    @PostMapping ("/login")
    public Mono <WsResponse <LoginResponse>> login (@RequestBody @Valid LoginRequest request){

        return authService.login(request);
    }

@GetMapping("/me")
public Mono<WsResponse<UserResponse>> me(@AuthenticationPrincipal Object principal) {
    if (principal == null) {
        System.out.println("Principal is null in AuthController.me");
        return Mono.error(new IllegalStateException("No authenticated principal found"));
    }

    String email = null;

    if (principal instanceof Jwt jwt) {
        // Use email claim if it exists, otherwise fallback to "sub"
        email = jwt.getClaimAsString("email");
        if (email == null) {
            email = jwt.getSubject(); // usually the sub claim
        }
        System.out.println("Principal is JWT. Extracted email: " + email);
    } else if (principal instanceof OAuth2User oauth2User) {
        email = oauth2User.getAttribute("email");
        System.out.println("Principal is OAuth2User. Extracted email: " + email);
    } else {
        System.out.println("Unsupported principal type: " + principal.getClass().getName());
        return Mono.error(new IllegalStateException("Unsupported principal type: " + principal.getClass().getName()));
    }

    if (email == null) {
        return Mono.error(new IllegalStateException("Cannot determine email from principal"));
    }

    return authService.getUserByEmail(email);
}

}
