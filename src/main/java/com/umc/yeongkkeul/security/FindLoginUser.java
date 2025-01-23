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
        /*
        if (authentication != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                // UserDetails 타입인 경우
                String userName = authentication.getName();
                log.error("user의 이메일은 " + userName);
                if (userRepository.existsByEmail(userName)) {
                    String memberEmail = userRepository.findByEmail(userName).get().getEmail();
                    log.error("user의 이메일은 " + memberEmail);
                    return memberEmail;
                }
            } else if (principal instanceof String) {
                // String 타입인 경우 (예: 이메일)
                String userName = (String) principal;
                log.error("user의 이메일은 " + userName);
                if (userRepository.existsByEmail(userName)) {
                    String memberEmail = userRepository.findByEmail(userName).get().getEmail();
                    log.error("user의 이메일은 " + memberEmail);
                    return memberEmail;
                }
            }
        }
         */
        return null;
    }
}
