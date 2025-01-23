package com.umc.yeongkkeul.security;

import com.umc.yeongkkeul.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class FindLoginUser {
    private static UserRepository userRepository;

    @Autowired
    public FindLoginUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if ((authentication != null)) {
            String userName = authentication.getName();
            if (userRepository.existsByEmail(userName)) {
                String userEmail = userRepository.findByEmail(userName).get().getEmail();
                return userEmail;
            }

        }
        return null;
    }
}
