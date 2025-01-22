package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.security.FindLoginUser;
import com.umc.yeongkkeul.security.TokenProvider;
import com.umc.yeongkkeul.service.AuthCommandService;
import com.umc.yeongkkeul.service.KakaoLoginService;
import com.umc.yeongkkeul.service.UserService;
import com.umc.yeongkkeul.web.dto.KakaoInfoResponseDto;
import com.umc.yeongkkeul.web.dto.TokenDto;
import com.umc.yeongkkeul.web.dto.TokenRequestDto;
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
    @Autowired
    private AuthCommandService authCommandService;
    @Autowired
    private UserService userService;

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

        TokenDto tokenDto = tokenProvider.genrateToken(email);

        String jwtToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();

        KakaoInfoResponseDto.KakaoInfoDTO kakaoInfoDTO = kakaoLoginService.loginUserInfo(jwtToken,refreshToken,email,name);

        return ApiResponse.onSuccess(kakaoInfoDTO);
    }


    //구글




    //사용자 정보기입
    @PutMapping("/auth/user-info")
    @Operation(summary = "사용자 정보 기입", description = "사용자 정보기입 소셜 로그인 후 기본 정보 저장하고 PUT으로 값 추가하는 형태")
    public ApiResponse<?> saveUserInfo(@RequestBody UserRequestDto.userInfoDto userInfoDto){

        String email = FindLoginUser.getCurrentUserId();
        log.info("email: {}", email);
        userService.saveUserInfo(email,userInfoDto);

        return ApiResponse.onSuccess("정보기입 성공");

    }

    //약관동의
    @PostMapping("/auth/term-agreement")
    @Operation(summary = "약관 동의", description = "Term1~Term3(필수),Term4(선택)")
    public ApiResponse<?> saveUserTerms(@RequestBody UserRequestDto.TermDTO termDto){
        String email = FindLoginUser.getCurrentUserId();
        userService.saveUserTerms(email,termDto);

        log.info("emailCheck: {}", email);

        return ApiResponse.onSuccess("약관 동의 성공");
    }

    @Operation(summary = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        return ResponseEntity.ok(authCommandService.reissue(tokenRequestDto));
    }

}
