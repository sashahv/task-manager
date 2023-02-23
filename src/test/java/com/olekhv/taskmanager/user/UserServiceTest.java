package com.olekhv.taskmanager.user;

import com.olekhv.taskmanager.exception.NoPermissionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private User user;
    private User admin;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("testUser@gmail.com")
                .role(Role.USER)
                .build();

        admin = User.builder()
                .firstName("Test")
                .lastName("Admin")
                .email("testAdmin@gmail.com")
                .role(Role.ADMIN)
                .build();

        when(userRepository.findByEmail("testUser@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("testAdmin@gmail.com")).thenReturn(Optional.of(admin));
    }

    @Test
    void should_change_role_of_user_if_admin_account() {
        userService.changeUserRole(Role.ADMIN, user.getEmail(), admin.getEmail());

        verify(userRepository, times(1)).save(user);

        assertEquals(Role.ADMIN, user.getRole());
    }

    @Test
    void should_throw_exception_if_account_that_change_role_is_not_admin() {
        assertThrows(NoPermissionException.class, () ->
                userService.changeUserRole(Role.ADMIN, user.getEmail(), user.getEmail())
        );
    }

    @Test
    void should_change_user_information(){
        userService.editUserInformation("Edited", "User", user.getEmail());

        verify(userRepository, times(1)).save(user);

        assertEquals("Edited", user.getFirstName());
        assertEquals("User", user.getLastName());
    }

    @Test
    void should_change_password(){
        String newPassword = "newPassword";

        when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn(newPassword);

        userService.changeUserPassword(
                "oldPassword", newPassword, newPassword, user.getEmail()
        );

        verify(userRepository, times(1)).save(user);

        assertEquals(newPassword, user.getPassword());
    }

    @Test
    void should_throw_exception_if_old_password_is_not_confirmed(){
        when(passwordEncoder.matches("oldPassword", user.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                userService.changeUserPassword(
                        "oldPassword", "12345", "12345", user.getEmail()
        ));
    }

    @Test
    void should_throw_exception_if_new_password_is_not_confirmed(){
        assertThrows(IllegalArgumentException.class, () ->
                userService.changeUserPassword(
                        "oldPassword", "newPassword", "editedNewPassword", user.getEmail()
                )
        );
    }
}