package com.dinoventures.backend.util;

import com.dinoventures.backend.model.User;
import com.dinoventures.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationUtil {

    private final UserRepository userRepository;

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("Authentication not found in security context");
            throw new IllegalStateException("User not authenticated");
        }

        String email = getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("Cannot extract user email from authentication");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        log.debug("Current authenticated user ID: {}", user.getId());
        return user.getId();
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }

        log.warn("Cannot extract email from authentication");
        return null;
    }

    public User getCurrentUser() {
        String email = getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("User not authenticated");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
