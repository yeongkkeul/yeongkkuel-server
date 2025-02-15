package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatUserProfileDto {
    private Long userId;
    private String nickname;
    private String profileImage;
    private String createdAt;
    private AgeGroup age;
    private Job job;
    private int rank;
}
