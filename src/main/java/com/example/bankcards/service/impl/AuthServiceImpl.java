package com.example.bankcards.service.impl;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.auth.AuthDto;
import com.example.bankcards.dto.auth.UserDto;
import com.example.bankcards.dto.mapper.UserMapper;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtProvider;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.util.RestConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final UserMapper userMapper;

    @Override
    public ApiResponse login(AuthDto.LoginDto dto) {
        User user = this.userRepository.findByUsernameAndDeletedFalse(dto.getUsername())
                .orElseThrow(() -> new UserNotFoundException(RestConstants.USER_NOT_FOUND));
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        try {
            this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    dto.getUsername(),
                    dto.getPassword()
            ));
        } catch (Exception e) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .data(e.getMessage())
                    .build();
        }
        String token = this.jwtProvider.generateAccessToken(userDetails.getUsername());
        return ApiResponse
                .builder()
                .status(HttpStatus.OK)
                .message(RestConstants.SUCCESS)
                .data(this.userMapper.toDto(user))
                .meta(Map.of(
                        "Token", token,
                        "expiredAt", this.jwtProvider.getAccessTokenExpiredDate(token)
                ))
                .build();
    }

    @Override
    public ApiResponse registerEmployee(UserDto.RegisterEmployeeDto dto) {
        Optional<User> userOptional = this.userRepository.findByUsername(dto.getUsername());
        if (userOptional.isPresent()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("User is already registered with this username: " + dto.getUsername())
                    .build();
        }
        Role requestedRole = Role.valueOf(dto.getRole().name());
        User user = this.userMapper.toEntity(dto, requestedRole);

        user.setUserStatus(UserStatus.ACTIVE);

        user.setPassword(this.passwordEncoder.encode(dto.getPassword()));
        User saved = this.userRepository.save(user);

        UserDto userDto = this.userMapper.toDto(saved);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message(RestConstants.USER_SAVED)
                .data(userDto)
                .build();
    }

    @Transactional
    @Override
    public ApiResponse changeCredentials(AuthDto.CreateCredentialDto dto, Long id) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        User existingUser = this.userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(RestConstants.USER_NOT_FOUND));

        if (!existingUser.getUsername().equals(currentUser)) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message(RestConstants.FAILED_TO_UPDATE)
                    .build();
        }
        System.out.println("Before Save: " + existingUser);
        this.userRepository.save(existingUser);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message(RestConstants.USER_UPDATED)
//                .data(credentialDto)
                .build();
    }


    @Override
    public ApiResponse changePassword(AuthDto.PasswordDto dto, Long id) {

        User targetUser = this.userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new UserNotFoundException(RestConstants.USER_NOT_FOUND));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = this.userRepository.findByUsernameAndDeletedFalse(currentUser)
                .orElseThrow(() -> new UserNotFoundException(RestConstants.USER_NOT_FOUND));

        if (!targetUser.getId().equals(user.getId())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("You can change only your password!")
                    .build();
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("New password is not entered")
                    .build();
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("old password is wrong!")
                    .build();
        }
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Old password cannot be the same with new one!")
                    .build();
        }
        if (dto.getNewPassword() == null || dto.getNewPassword().trim().isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("New password place cannot be empty!")
                    .build();
        }

        if (dto.getNewPassword().length() < 6) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("New password should be at least 6!")
                    .build();
        }

        user.setPassword(this.passwordEncoder.encode(dto.getNewPassword()));
        this.userRepository.save(user);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message(RestConstants.PASSWORD_SUCCESSFULLY_UPDATED)
                .build();
    }

    @Transactional
    @Override
    public ApiResponse updateEmployeeByAdmin(UserDto.UpdateEmployeeDto dto) {

        User user = this.userRepository.findByIdAndDeletedFalse(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException(RestConstants.USER_NOT_FOUND));


        if (dto.getFullName() != null && !dto.getFullName().trim().isEmpty()) {
            user.setFullName(dto.getFullName());
        }


        User saved = this.userRepository.save(user);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message(RestConstants.USER_UPDATED)
                .data(saved)
                .build();
    }

}
