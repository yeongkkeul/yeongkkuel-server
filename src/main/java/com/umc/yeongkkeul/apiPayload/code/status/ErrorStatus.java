package com.umc.yeongkkeul.apiPayload.code.status;

import com.umc.yeongkkeul.apiPayload.code.BaseErrorCode;
import com.umc.yeongkkeul.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // For test
    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP4001", "테스트 용도"),

    // User
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER4001", "사용자를 찾을 수 없습니다."),

    // Category
    CATEGORY_DUPLICATE(HttpStatus.CONFLICT, "CATEGORY4004", "동일한 카테고리가 이미 존재합니다."),
    CATEGORY_NO_PERMISSION(HttpStatus.FORBIDDEN, "CATEGORY4002", "해당 카테고리에 대한 접근 권한이 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "CATEGORY4001", "카테고리를 찾을 수 없습니다."),

    //JWT
    _INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "USER4003", "유효하지 않은 토큰입니다."),
    _INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "USER4004", "유효하지 않은 토큰입니다."),

    //USER
    _USER_NOT_FOUND(HttpStatus.NOT_FOUND,"USER4005","사용자를 찾을 수 없습니다."),
    _REFERRALCODE_NOT_FOUND(HttpStatus.NOT_FOUND,"USER4004","존재하지 않은 추천인 코드입니다."),

    // CHATROOM
    _CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND,"CHATROOM4004","채팅방을 찾을 수 없습니다."),

    // Notification
    _NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION4004", "해당 사용자의 알림을 찾을 수 없습니다."),

    // Expense
    EXPENSE_NOT_FOUND(HttpStatus.BAD_REQUEST, "EXPENSE4001", "지출 내역을 찾을 수 없습니다."),
    EXPENSE_AMOUNT_ERROR(HttpStatus.BAD_REQUEST, "EXPENSE4002", "지출 금액이 유효하지 않습니다.."),
    EXPENSE_CATEGORY_NOT_FOUND(HttpStatus.BAD_REQUEST, "EXPENSE4002", "유효하지 않은 지출 카테고리입니다."),
    EXPENSE_DAY_TARGET_EXPENDITURE_ERROR(HttpStatus.BAD_REQUEST, "EXPENSE4004", "하루 목표 지출액이 유효하지 않습니다."),
    EXPENSE_DAY_TARGET_EXPENDITURE_NOT_FOUND(HttpStatus.BAD_REQUEST, "EXPENSE4005", "하루 목표 지출액이 존재하지 않습니다."),

    // Purchase (아이템 이력)
    Purchase_NOT_FOUND(HttpStatus.BAD_REQUEST, "ITEMH4001", "아이템 이력 기록이 존재하지 않습니다."),
    _PURCHASE_NOT_FOUND(HttpStatus.NOT_FOUND,"ITEM4004","구매한 아이템이 아닙니다."),
    // Reward
    _NOT_ENOUGH_REWARD(HttpStatus.BAD_REQUEST,"REWARD4001","보유 리워드 부족합니다."),

    //Item
    _ITEM_NOT_FOUND(HttpStatus.NOT_FOUND,"ITEM4001", "현재 스킨이 존재하지 않습니다.");




    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}
