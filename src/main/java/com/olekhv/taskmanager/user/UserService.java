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

    public User fetchUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException("User " + email + " not found")
        );
    }

    public void editUserInformation(String newFirstName,
                                    String newLastName,
                                    String email) {
        User user = fetchUserByEmail(email);
        user.setFirstName(newFirstName);
        user.setLastName(newLastName);

        userRepository.save(user);
    }

    public void changeUserPassword(String oldPassword,
                                   String newPassword,
                                   String confirmedPassword,
                                   String email) {
        User user = fetchUserByEmail(email);

        if (!oldPassword.equals(user.getPassword())) {
            /* !oldPassword.equals(user.getPassword()) is used
            for password recovery, where old password is set as user.getPassword()*/

            if (!isOldPasswordCorrect(user.getPassword(), oldPassword)) {
                throw new IllegalArgumentException("Old password is incorrect");
            }
        }

        if (!isVerifiedNewPassword(newPassword, confirmedPassword)) {
            throw new IllegalArgumentException("New password is not confirmed");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    public boolean isVerifiedNewPassword(String newPassword,
                                         String confirmedPassword) {
        return newPassword.equals(confirmedPassword);
    }

    private boolean isOldPasswordCorrect(String oldPassword, String password) {
        return passwordEncoder.matches(oldPassword, password);
    }

    public void changeUserRole(Role role, String email, String adminEmail) {
        User user = fetchUserByEmail(email);

        User admin = userRepository.findByEmail(adminEmail).orElseThrow(
                () -> new UsernameNotFoundException("User " + adminEmail + " not found")
        );

        if (!admin.getRole().equals(Role.ADMIN)) {
            throw new NoPermissionException("No permission");
        }

        user.setRole(role);
        userRepository.save(user);
    }
}
