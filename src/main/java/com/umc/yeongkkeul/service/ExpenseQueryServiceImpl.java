package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.ExpenseHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseQueryServiceImpl implements ExpenseQueryService {

    private final UserRepository userRepository;

    // 일간 - 유저의 하루 목쵸 지출액 조회
    @Override
    public ExpenseResponseDTO.DayTargetExpenditureViewDTO DayTargetExpenditureViewDTO(String userEmail) {
        // 유저 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 만약 유저가 하루 목표 지출액을 설정해둔 적이 없다면 에러
        if (user.getDayTargetExpenditure() == null) {
            throw new ExpenseHandler(ErrorStatus.EXPENSE_DAY_TARGET_EXPENDITURE_NOT_FOUND);
        }

        return new ExpenseResponseDTO.DayTargetExpenditureViewDTO().builder()
                .dayTargetExpenditure(user.getDayTargetExpenditure())
                .build();
    }
}
