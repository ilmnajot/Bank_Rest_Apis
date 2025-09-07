package com.example.bankcards.controller.auth;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.auth.UserDto;
import com.example.bankcards.dto.auth.AuthDto;
import com.example.bankcards.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auths")
public class AuthController {
    private final AuthService authService;


//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register-employee")
    public HttpEntity<ApiResponse> registerEmployee(@RequestBody UserDto.RegisterEmployeeDto dto) {
        ApiResponse apiResponse = this.authService.registerEmployee(dto);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/change-credentials/{id}")
    public HttpEntity<ApiResponse> changeCredentials(@PathVariable(value = "id") Long id,
                                                     @RequestBody AuthDto.ChangeCredentialDto dto) {
        ApiResponse apiResponse = this.authService.changeCredentials(dto, id);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }


//    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/login")
    public HttpEntity<ApiResponse> login(@RequestBody AuthDto.LoginDto dto) {
        ApiResponse apiResponse = this.authService.login(dto);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PutMapping("/change-password")
    public HttpEntity<ApiResponse> changePassword(
            @RequestBody AuthDto.PasswordDto dto,
            @RequestParam("id") Long id) {
        ApiResponse apiResponse = this.authService.changePassword(dto, id);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/update-employee-by-admin")
    public HttpEntity<ApiResponse> updateEmployeeByAdmin(@RequestParam(value = "userId") Long userId,
                                                             @RequestBody UserDto.UpdateEmployeeDto dto) {
        ApiResponse apiResponse = this.authService.updateEmployeeByAdmin(dto, userId);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/get-user/{userId}")
    public ApiResponse getUserById(@PathVariable Long userId){
        return this.authService.getUserById(userId);
    }

}
