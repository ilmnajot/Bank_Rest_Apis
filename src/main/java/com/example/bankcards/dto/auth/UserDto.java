package com.example.bankcards.dto.auth;


import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserDto {

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
    public static class RegisterEmployeeDto {

        private String fullName;
        private String username;
        private String password;
        private Role role;
        private UserStatus userStatus;

    }
        @Data
        public static class UpdateEmployeeDto {

            private String fullName;
            private String username;
            private UserStatus userStatus;

        }

}
