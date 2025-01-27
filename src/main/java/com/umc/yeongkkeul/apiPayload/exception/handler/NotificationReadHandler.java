package com.umc.yeongkkeul.apiPayload.exception.handler;

import com.umc.yeongkkeul.apiPayload.code.BaseErrorCode;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;

public class NotificationReadHandler extends GeneralException {

    public NotificationReadHandler(BaseErrorCode errorCode) { super(errorCode); }
}
