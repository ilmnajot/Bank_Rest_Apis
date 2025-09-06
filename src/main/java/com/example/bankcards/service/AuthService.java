package com.example.bankcards.service;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.auth.UserDto;
import com.example.bankcards.dto.auth.AuthDto;
import org.springframework.stereotype.Component;

@Component
public interface AuthService {

    ApiResponse registerEmployee(UserDto.RegisterEmployeeDto dto);

    ApiResponse changeCredentials(AuthDto.CreateCredentialDto dto, Long id);

    ApiResponse login(AuthDto.LoginDto dto);


    ApiResponse changePassword(AuthDto.PasswordDto dto, Long id);

    ApiResponse updateEmployeeByAdmin(UserDto.UpdateEmployeeDto dto);
}
