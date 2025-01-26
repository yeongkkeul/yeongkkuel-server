package com.umc.yeongkkeul.security;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
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

    public static Long toId(String email){
        return userRepository.findByEmail(email).orElseThrow(()->new UserHandler(ErrorStatus.USER_NOT_FOUND)).getId();
    }


}
