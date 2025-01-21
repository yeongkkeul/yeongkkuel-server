package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.security.TokenProvider;
import com.umc.yeongkkeul.service.KakaoLoginService;
import com.umc.yeongkkeul.web.dto.KakaoInfoResponseDto;
import com.umc.yeongkkeul.web.dto.UserRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class UserController {

    @Autowired
    private KakaoLoginService kakaoLoginService;

    @Autowired
    private TokenProvider tokenProvider;

    //카카오
    @GetMapping("/auth/kakao-login/")
    @Operation(summary = "카카오 로그인", description = "카카오 로그인 GET")
    public ApiResponse<KakaoInfoResponseDto.KakaoInfoDTO> kakakoLogin(@RequestParam String code){
        String accessToken = kakaoLoginService.getKakaoAccessToken(code);

        HashMap<String, Object> userInfo = kakaoLoginService.getUserInfo(accessToken);

        String email = userInfo.get("email").toString();
        String name = userInfo.get("nickname").toString();
        if (email == null) {
            return ApiResponse.onFailure("4000", "Email not found in Kakao account", null);
        }

        String jwtToken = tokenProvider.genrateToken(email).getAccessToken();
        String refreshToken = tokenProvider.genrateToken(email).getRefreshToken();

        KakaoInfoResponseDto.KakaoInfoDTO kakaoInfoDTO = kakaoLoginService.loginUserInfo(jwtToken,email,name);

        return ApiResponse.onSuccess(kakaoInfoDTO);
    }


    //구글




    //사용자 정보기입
//    @PutMapping("/auth/user-info")
//    @Operation(summary = "사용자 정보 기입", description = "사용자 정보기입 소셜 로그인 후 기본 정보 저장하고 PUT으로 값 추가하는 형태")
//    public ApiResponse<?> saveUserInfo(@RequestBody UserRequestDto.userInfoDto userInfoDto){
//
//    }

}
