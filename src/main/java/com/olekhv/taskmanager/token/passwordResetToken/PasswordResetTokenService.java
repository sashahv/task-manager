package com.olekhv.taskmanager.token.passwordResetToken;

import com.olekhv.taskmanager.exception.TokenNotFoundException;
import com.olekhv.taskmanager.user.User;
import com.olekhv.taskmanager.user.UserRepository;
import com.olekhv.taskmanager.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final UserService userService;


    public String createPasswordResetToken(String email){
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User " + email + " not found")
        );

        String token = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        passwordResetTokenRepository.save(passwordResetToken);
        return token;
    }

    public boolean isValidPasswordResetToken(String token){
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);

        if(passwordResetToken.isPresent()){
            Duration timeBetweenNowAndTokenExpiration =
                    Duration.between(LocalDateTime.now(), passwordResetToken.get().getExpirationTime());

            if(timeBetweenNowAndTokenExpiration.isNegative() || timeBetweenNowAndTokenExpiration.isZero()){
                passwordResetTokenRepository.delete(passwordResetToken.get());
            }
        }

        return passwordResetToken.isPresent();
    }

    public void recoverPasswordByToken(String newPassword,
                                       String passwordConfirmation,
                                       String token){
        if(!isValidPasswordResetToken(token)){
            throw new TokenNotFoundException("Token is invalid");
        }

        if(!userService.isVerifiedNewPassword(newPassword, passwordConfirmation)){
            throw new RuntimeException("Password is not confirmed");
        }

        User user = fetchUserByResetToken(token);

        userService.changeUserPassword(user.getPassword(),
                newPassword,
                passwordConfirmation,
                user.getEmail());
    }

    public User fetchUserByResetToken(String token){
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);

        if(passwordResetToken.isEmpty()) throw new UsernameNotFoundException("User not found");

        return passwordResetToken.get().getUser();
    }
}
