package com.olekhv.taskmanager.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordDTO {
    private String oldPassword;
    private String newPassword;
    private String passwordConfirmation;
}
