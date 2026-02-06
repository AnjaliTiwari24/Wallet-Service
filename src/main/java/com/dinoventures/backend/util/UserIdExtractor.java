package com.dinoventures.backend.util;

import com.dinoventures.backend.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserIdExtractor {

    public Long extractUserIdFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return ((User) authentication.getPrincipal()).getId();
        }
        
        // Fallback: try to extract from authentication name
        if (authentication != null && authentication.getName() != null) {
            // This requires a lookup - in production scenarios
            return null;
        }
        
        return null;
    }

    public User extractUserFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        
        return null;
    }
}
