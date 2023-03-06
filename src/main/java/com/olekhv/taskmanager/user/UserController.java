package com.olekhv.taskmanager.user;

import com.olekhv.taskmanager.token.passwordResetToken.PasswordRecoveryDTO;
import com.olekhv.taskmanager.token.passwordResetToken.PasswordResetTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final PasswordResetTokenService passwordResetTokenService;


    @PutMapping("/edit")
    public ResponseEntity<String> editUserInformation(@RequestParam(value = "firstName", required = false) String firstName,
                                                      @RequestParam(value = "lastName", required = false) String lastName,
                                                      @AuthenticationPrincipal UserDetails userDetails){
        userService.editUserInformation(firstName, lastName, userDetails.getUsername());
        return ResponseEntity.ok("User information was successfully edited");
    }

    @PutMapping("/edit/password")
    public ResponseEntity<String> changeUserPassword(@RequestBody PasswordDTO passwordDTO,
                                                     @AuthenticationPrincipal UserDetails userDetails){
        userService.changeUserPassword(passwordDTO.getOldPassword(),
                passwordDTO.getNewPassword(),
                passwordDTO.getPasswordConfirmation(),
                userDetails.getUsername());
        return ResponseEntity.ok("Password has been changed");
    }

    @PutMapping("/edit/role")
    public ResponseEntity<String> changeRole(@RequestParam("role") Role role,
                                             @RequestParam("userEmail") String email,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        userService.changeUserRole(role, email, userDetails.getUsername());
        return ResponseEntity.ok("Role of " + email + " was changed to " + role.name());
    }

    @PutMapping("/password/reset")
    public ResponseEntity<String> resetPassword(@RequestParam("userEmail") String email,
                                                HttpServletRequest request){
        String passwordResetToken = passwordResetTokenService.createPasswordResetToken(email);

        return ResponseEntity.ok(getPasswordRecoveryLink(passwordResetToken, getApplicationUrl(request)));
    }

    @PutMapping("/password/recovery")
    private ResponseEntity<String> recoverPasswordByToken(String token,
                                                          @RequestBody PasswordRecoveryDTO passwordRecoveryDTO){
        passwordResetTokenService.recoverPasswordByToken(passwordRecoveryDTO.getNewPassword(),
                passwordRecoveryDTO.getPasswordConfirmation(),
                token);
        return ResponseEntity.ok("Password was changed successfully");
    }

    private String getPasswordRecoveryLink(String token,
                                           String applicationUrl){
        return applicationUrl + "/api/v1/users/password/recovery?token=" + token;
    }

    private String getApplicationUrl(HttpServletRequest request){
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
