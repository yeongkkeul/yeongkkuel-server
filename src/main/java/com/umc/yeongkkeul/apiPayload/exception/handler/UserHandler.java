package com.umc.yeongkkeul.apiPayload.exception.handler;

import com.umc.yeongkkeul.apiPayload.code.BaseErrorCode;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;

public class UserHandler extends GeneralException {
    public UserHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}