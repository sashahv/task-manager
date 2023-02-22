package com.olekhv.taskmanager.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PutMapping("/edit")
    public ResponseEntity<String> editUserInformation(@RequestParam(value = "firstName", required = false) String firstName,
                                                      @RequestParam(value = "lastName", required = false) String lastName,
                                                      @AuthenticationPrincipal UserDetails userDetails){
        userService.editUserInformation(firstName, lastName, userDetails.getUsername());
        return ResponseEntity.ok("User information was successfully edited");
    }

    @PutMapping("/edit/password")
    public ResponseEntity<String> changeUserPassword(@RequestParam(value = "oldPassw") String oldPassword,
                                                     @RequestParam(value = "newPassw") String newPassword,
                                                     @RequestParam(value = "confNewPassw") String confirmedNewPassword,
                                                     @AuthenticationPrincipal UserDetails userDetails){
        userService.changeUserPassword(oldPassword, newPassword, confirmedNewPassword, userDetails.getUsername());
        return ResponseEntity.ok("Password has been changed");
    }

    @PutMapping("/edit/role")
    public ResponseEntity<String> changeRole(@RequestParam("role") Role role,
                                             @RequestParam("userEmail") String email,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        userService.changeUserRole(role, email, userDetails.getUsername());
        return ResponseEntity.ok("Role of " + email + " was changed to " + role.name());
    }
}
