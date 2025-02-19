package com.umc.yeongkkeul.service;


import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import com.umc.yeongkkeul.repository.ChatRoomMembershipRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.ChatUserProfileDto;
import com.umc.yeongkkeul.web.dto.ChatUserRankResponseDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
@RequiredArgsConstructor
@Slf4j
public class UserRankService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRoomMembershipRepository chatRoomMembershipRepository;

    public ChatUserRankResponseDto.chatRankListDto chatRankListDto(Long chatRoomId) {

        List<ChatRoomMembership> chatRoomList = chatRoomMembershipRepository.findByChatroomIdOrderByUserScoreDesc(chatRoomId);

        List<ChatUserRankResponseDto.userInfoDto> userRanks =
                chatRoomList.stream()
                        .map(membership -> {
                            User user = membership.getUser();  // ChatRoomMembership에 User 정보가 있다고 가정
                            return ChatUserRankResponseDto.userInfoDto.builder()
                                    .userId(user.getId())
                                    .nickname(user.getNickname())
                                    .profileImage(user.getImageUrl())  // User 엔티티에 profileImage 필드가 있다고 가정
                                    .rankScore(membership.getUserScore())
                                    .rank(chatRoomList.indexOf(membership) + 1) // 리스트 순서 기반으로 랭킹 부여
                                    .build();
                        })
                        .collect(Collectors.toList());


        return new ChatUserRankResponseDto.chatRankListDto(userRanks);
    }


    public ChatUserProfileDto chatUserProfile(Long chatRoomId, Long userId) {

        List<ChatRoomMembership> yesterdayRankList = chatRoomMembershipRepository.findByChatroomIdOrderByYesterdayScoreDesc(chatRoomId);

        ChatRoomMembership findUserProfile = chatRoomMembershipRepository.findByChatroom_IdAndUser_Id(chatRoomId,userId)
                .orElseThrow(()->new GeneralException(ErrorStatus._USER_NOT_FOUND));



        User user = findUserProfile.getUser();

        int rank = yesterdayRankList.indexOf(findUserProfile) + 1;

        String formattedCreatedAt = findUserProfile.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"));

        return ChatUserProfileDto.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImage(user.getImageUrl())
                .createdAt(formattedCreatedAt)
                .age(user.getAgeGroup())
                .job(user.getJob())
                .rank(rank)
                .build();


    }
}
