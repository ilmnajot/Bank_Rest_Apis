package com.example.bankcards.dto.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.auth.AuthDto;
import com.example.bankcards.dto.auth.UserDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.util.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class UserMapper {

    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .role(user.getRole())
                .userStatus(user.getUserStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .deleted(user.getDeleted())
                .build();
    }

    public User toEntity(UserDto.RegisterEmployeeDto dto, Role role) {
        if (dto == null) return null;
        return User.builder()
                .fullName(dto.getFullName())
                .username(dto.getUsername())
                .role(role)
                .userStatus(dto.getUserStatus())
                .build();
    }

    public void toUpdate(AuthDto.ChangeCredentialDto dto, User user) {
        if (dto == null) return;

        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
            user.setUsername(dto.getUsername());
        }
    }
    public void toUpdate(UserDto.UpdateEmployeeDto dto, User user) {
        if (dto == null) return;

        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getUsername() != null && !dto.getUsername().trim().isEmpty()) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getUserStatus()!=null){
            user.setUserStatus(dto.getUserStatus());
        }
    }

}
