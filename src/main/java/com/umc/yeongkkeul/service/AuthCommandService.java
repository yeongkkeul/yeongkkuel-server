package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.security.TokenProvider;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Data
@RequiredArgsConstructor
public class AuthCommandService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenProvider tokenProvider;




}
