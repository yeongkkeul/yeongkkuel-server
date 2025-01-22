package com.umc.yeongkkeul.security;

import com.umc.yeongkkeul.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FindLoginUser {
    private static UserRepository userRepository;

    @Autowired
    public FindLoginUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public static String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        /*
        JWT를 사용하는 경우, UsernamePasswordAuthenticationToken의 principal은 이메일 같은 문자열이 들어가게 될 수 있다고 합니다.
        이 경우 authentication.getPrincipal()은 String 타입이므로 UserDetails로 캐스팅할 수 없다고 하여 수정해보았습니다..!

        if ((authentication != null) && (authentication.getPrincipal() instanceof UserDetails)) {
            String userName = authentication.getName();
            log.error("user의 이름은 "+ userName);
            if (userRepository.existsByEmail(userName)) {
                String memberEmail = userRepository.findByEmail(userName).get().getEmail();
                log.error("user의 이메일은 "+ memberEmail);
                return memberEmail;
            }

        }
         */
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
        return null;
    }
}
