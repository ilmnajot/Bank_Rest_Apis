package com.example.bankcards.dto.auth;

import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthDto {

    private Long id;

    private String fullName;
    private String username;
    private Role role;
    private UserStatus userStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Boolean deleted;

    @Data
    public static class CreateCredentialDto {
        private String fullName;
        private String username;
        private Role role;
        private UserStatus userStatus;
    }
    @Data
    public static class ChangeCredentialDto {
        private String fullName;
        private String username;
    }

    @Data
    public static class LoginDto {
        private String username;
        private String password;
    }

    @Data
    public static class PasswordDto {
        private String oldPassword;
        private String newPassword;
    }
}
