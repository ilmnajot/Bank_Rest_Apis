package com.example.bankcards.service;

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
import com.example.bankcards.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
//                .id(1L)
                .fullName("John Doe")
                .username("johndoe")
                .password("encodedPassword")
                .role(Role.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
        testUser.setId(1L);

        SecurityContextHolder.setContext(securityContext);
    }

    // ============= LOGIN TESTS =============

    @Test
    @DisplayName("Should login successfully")
    void testLogin_Success() {
        AuthDto.LoginDto loginDto = new AuthDto.LoginDto();
        loginDto.setUsername("johndoe");
        loginDto.setPassword("password123");

        when(userRepository.findByUsernameAndDeletedFalse("johndoe")).thenReturn(Optional.of(testUser));
        when(userDetailsService.loadUserByUsername("johndoe")).thenReturn(testUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtProvider.generateAccessToken("johndoe")).thenReturn("token123");
        when(jwtProvider.getAccessTokenExpiredDate("token123")).thenReturn(new Date());
        when(userMapper.toDto(testUser)).thenReturn(new UserDto(
                testUser.getId(),
                testUser.getFullName(),
                testUser.getUsername(),
                testUser.getRole(),
                testUser.getUserStatus(),
                testUser.getCreatedAt()
                ,testUser.getUpdatedAt(),
                testUser.getCreatedBy(),
                testUser.getUpdatedBy(),
                testUser.getDeleted()));

        ApiResponse response = authService.login(loginDto);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("SUCCESS", response.getMessage());
        assertEquals(testUser.getFullName(), ((UserDto) response.getData()).getFullName());
        assertTrue(((Map<String, Object>) response.getMeta()).containsKey("Token"));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when username not found")
    void testLogin_UserNotFound() {
        AuthDto.LoginDto loginDto = new AuthDto.LoginDto();
        loginDto.setUsername("unknown");
        loginDto.setPassword("password");

        when(userRepository.findByUsernameAndDeletedFalse("unknown")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(loginDto));
    }

    // ============= REGISTER EMPLOYEE TESTS =============

    @Test
    @DisplayName("Should register employee successfully")
    void testRegisterEmployee_Success() {
        UserDto.RegisterEmployeeDto dto = new UserDto.RegisterEmployeeDto();
        dto.setUsername("johndoe");
        dto.setPassword("password123");
        dto.setRole(Role.USER);

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.empty());
        when(userMapper.toEntity(dto, Role.USER)).thenReturn(testUser);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(new UserDto(
                testUser.getId(),
                testUser.getFullName(),
                testUser.getUsername(),
                testUser.getRole(),
                testUser.getUserStatus(),
                testUser.getCreatedAt()
                ,testUser.getUpdatedAt(),
                testUser.getCreatedBy(),
                testUser.getUpdatedBy(),
                testUser.getDeleted()));
        ApiResponse response = authService.registerEmployee(dto);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("User has been saved!", response.getMessage());
        assertEquals(testUser.getId(), ((UserDto) response.getData()).getId());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when username already exists")
    void testRegisterEmployee_UserExists() {
        UserDto.RegisterEmployeeDto dto = new UserDto.RegisterEmployeeDto();
        dto.setUsername("johndoe");

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(testUser));

        ApiResponse response = authService.registerEmployee(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertTrue(response.getMessage().contains("User is already registered"));
        verify(userRepository, never()).save(any());
    }

    // ============= CHANGE PASSWORD TESTS =============

    @Test
    @DisplayName("Should change password successfully")
    void testChangePassword_Success() {
        AuthDto.PasswordDto dto = new AuthDto.PasswordDto();
        dto.setOldPassword("oldPass");
        dto.setNewPassword("newPass123");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsernameAndDeletedFalse("johndoe")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPass123", testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newPass123")).thenReturn("encodedNewPass");

        ApiResponse response = authService.changePassword(dto, 1L);

        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("PASSWORD SUCCESSFULLY UPDATED", response.getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when old password is wrong")
    void testChangePassword_WrongOldPassword() {
        AuthDto.PasswordDto dto = new AuthDto.PasswordDto();
        dto.setOldPassword("wrongOld");
        dto.setNewPassword("newPass123");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("johndoe");
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsernameAndDeletedFalse("johndoe")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongOld", testUser.getPassword())).thenReturn(false);

        ApiResponse response = authService.changePassword(dto, 1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("old password is wrong!", response.getMessage());
        verify(userRepository, never()).save(any());
    }

}
