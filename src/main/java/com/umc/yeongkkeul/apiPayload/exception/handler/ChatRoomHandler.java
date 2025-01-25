package com.umc.yeongkkeul.apiPayload.exception.handler;

import com.umc.yeongkkeul.apiPayload.code.BaseErrorCode;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;

public class ChatRoomHandler extends GeneralException {

    public ChatRoomHandler(BaseErrorCode errorCode) { super(errorCode); }
}
