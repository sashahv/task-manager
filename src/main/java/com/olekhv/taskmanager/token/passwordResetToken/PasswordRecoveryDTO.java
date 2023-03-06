package com.olekhv.taskmanager.token.passwordResetToken;

import lombok.Data;

@Data
public class PasswordRecoveryDTO {
    private String newPassword;
    private String passwordConfirmation;
}
