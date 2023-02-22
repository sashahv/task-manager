package com.olekhv.taskmanager.user;

import com.olekhv.taskmanager.exception.NoPermissionException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void editUserInformation(String newFirstName,
                                    String newLastName,
                                    String username){
        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User " + username + " not found")
        );

        user.setFirstName(newFirstName);
        user.setLastName(newLastName);

        userRepository.save(user);
    }

    public void changeUserPassword(String oldPassword,
                                   String newPassword,
                                   String confirmedPassword,
                                   String username){
        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User " + username + " not found")
        );

        if(!isOldPasswordCorrect(oldPassword, user.getPassword())){
            throw new IllegalArgumentException("Old password is incorrect");
        }

        if(!isVerifiedNewPassword(newPassword, confirmedPassword)){
            throw new IllegalArgumentException("New password is not confirmed");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    private boolean isVerifiedNewPassword(String newPassword,
                                          String confirmedPassword) {
        return newPassword.equals(confirmedPassword);
    }

    private boolean isOldPasswordCorrect(String oldPassword, String password) {
        return passwordEncoder.matches(oldPassword, password);
    }

    public void changeUserRole(Role role, String username, String adminEmail){
        User admin = userRepository.findByEmail(adminEmail).orElseThrow(
                () -> new UsernameNotFoundException("User " + adminEmail + " not found")
        );

        User user = userRepository.findByEmail(username).orElseThrow(
                () -> new UsernameNotFoundException("User " + username + " not found")
        );

        if(!admin.getRole().equals(Role.ADMIN)){
            throw new NoPermissionException("No permission");
        }

        user.setRole(role);
        userRepository.save(user);
    }
}
