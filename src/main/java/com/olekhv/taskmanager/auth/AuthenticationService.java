package com.olekhv.taskmanager.auth;

import com.olekhv.taskmanager.config.JwtService;
import com.olekhv.taskmanager.token.Token;
import com.olekhv.taskmanager.token.TokenRepository;
import com.olekhv.taskmanager.token.TokenType;
import com.olekhv.taskmanager.user.Role;
import com.olekhv.taskmanager.user.User;
import com.olekhv.taskmanager.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

    public AuthenticationResponse register(RegisterRequest request) {
        String requestEmail = request.getEmail();
        if(userRepository.findByEmail(requestEmail).isPresent()){
            throw new UsernameNotFoundException("User with email: " + requestEmail + " already exists");
        }
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(requestEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        User savedUser = userRepository.save(user);
        log.info(savedUser.toString());
        String jwt = jwtService.generateToken(user);
        saveUserToken(savedUser, jwt);
        return AuthenticationResponse.builder()
                .token(jwt)
                .build();
    }

    public AuthenticationResponse login(LoginRequest request) {
        String email = request.getEmail();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User with email: " + " does not exists")
                );
        String jwt = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwt);
        return AuthenticationResponse.builder()
                .token(jwt)
                .build();
    }

    private void saveUserToken(User user, String jwt) {
        Token token = Token.builder()
                .user(user)
                .token(jwt)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user){
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUserId(user.getId());
        if(validUserTokens.isEmpty()){
            return;
        }
        validUserTokens.forEach(
                token -> {
                    token.setExpired(true);
                    token.setRevoked(true);
                }
        );
        tokenRepository.saveAll(validUserTokens);
    }
}
