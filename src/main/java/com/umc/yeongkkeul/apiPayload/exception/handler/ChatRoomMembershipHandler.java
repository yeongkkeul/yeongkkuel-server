package com.umc.yeongkkeul.apiPayload.exception.handler;

import com.umc.yeongkkeul.apiPayload.code.BaseErrorCode;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;

public class ChatRoomMembershipHandler extends GeneralException {
    public ChatRoomMembershipHandler(BaseErrorCode errorCode) { super(errorCode); }
}
