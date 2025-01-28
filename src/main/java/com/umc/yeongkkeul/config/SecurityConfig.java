package com.umc.yeongkkeul.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.security.JwtAuthenticationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.PrintWriter;


@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig
{

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",   // Swagger 3 documentation endpoint
                                "/swagger-ui/**",     // Swagger UI endpoint
                                "/swagger-resources/**",
                                "/swagger-ui.html",
                                "/webjars/**",        // Webjars used by Swagger UI
                                "/api/auth/**",         // 카카오 로그인 엔드포인트
                                "/api/logout", // 로그아웃 엔드포인트
                                "/health" // health check용
                        ).permitAll() // Swagger 경로 허용
                        .requestMatchers("/ws/**").permitAll() // WebSocket 엔드포인트 허용
                        //.requestMatchers("/chat/**").permitAll() // WebSocket 엔드포인트 허용
                        .anyRequest().authenticated()
                )
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(unauthorizedEntryPoint) // 인증 실패 핸들러
                );

        // JWT 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private final AuthenticationEntryPoint unauthorizedEntryPoint =
            (request, response, authException) -> {

                ApiResponse<?> apiResponse = new ApiResponse(false,"401","인증이 필요합니다.",null);
                response.setCharacterEncoding("UTF-8");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                PrintWriter writer = response.getWriter();
                writer.write(new ObjectMapper().writeValueAsString(apiResponse));
                writer.flush();

            };

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}