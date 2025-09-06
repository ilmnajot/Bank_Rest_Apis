package com.example.bankcards.controller.auth;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.auth.UserDto;
import com.example.bankcards.dto.auth.AuthDto;
import com.example.bankcards.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auths")
public class AuthController {
    private final AuthService authService;


//    @PreAuthorize("hasAnyRole('HR','DEVELOPER','ADMIN')")
    @PostMapping("/register-employee")
    public HttpEntity<ApiResponse> registerEmployee(@RequestBody UserDto.RegisterEmployeeDto dto) {
        ApiResponse apiResponse = this.authService.registerEmployee(dto);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

//    @PreAuthorize("hasAnyRole('OWNER','PRINCIPAL','ADMIN', 'RECEPTION','HR','EDUCATIONAL_DEPARTMENT','DEVELOPER','MARKETING')")
    @PutMapping("/change-credentials/{id}")
    public HttpEntity<ApiResponse> changeCredentials(@PathVariable(value = "id") Long id,
                                                     @RequestBody AuthDto.CreateCredentialDto dto) {
        ApiResponse apiResponse = this.authService.changeCredentials(dto, id);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

//    @PreAuthorize("hasAnyRole('OWNER','PRINCIPAL','ADMIN', 'RECEPTION','HR','EDUCATIONAL_DEPARTMENT','RECEPTION','CASHIER','DEVELOPER')")
    @PostMapping("/login")
    public HttpEntity<ApiResponse> login(@RequestBody AuthDto.LoginDto dto) {
        ApiResponse apiResponse = this.authService.login(dto);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }

//    @PreAuthorize("hasAnyRole('OWNER','PRINCIPAL','ADMIN', 'RECEPTION','HR','EDUCATIONAL_DEPARTMENT','RECEPTION','CASHIER','DEVELOPER','MARKETING')")
    @PutMapping("/change-password")
    public HttpEntity<ApiResponse> changePassword(
            @RequestBody AuthDto.PasswordDto dto,
            @RequestParam("id") Long id) {
        ApiResponse apiResponse = this.authService.changePassword(dto, id);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }


//    @PreAuthorize("hasAnyRole('HR','DEVELOPER','ADMIN','MARKETING')")
    @PutMapping("/update-employee-by-admin")
    public HttpEntity<ApiResponse> updateEmployeeByAdmin(@RequestBody UserDto.UpdateEmployeeDto dto) {
        ApiResponse apiResponse = this.authService.updateEmployeeByAdmin(dto);
        return ResponseEntity.status(apiResponse.getStatus()).body(apiResponse);
    }
}
