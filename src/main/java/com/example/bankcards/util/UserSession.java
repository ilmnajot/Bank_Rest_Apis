package com.example.bankcards.util;

import com.example.bankcards.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserSession {


    public Long getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            log.info("Principal class {}", principal.getClass().getName());
            if (principal instanceof User user)
                return user.getId();
        }
        return null;
    }

}
