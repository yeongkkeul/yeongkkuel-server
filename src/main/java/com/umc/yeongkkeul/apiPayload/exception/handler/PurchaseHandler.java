package com.umc.yeongkkeul.apiPayload.exception.handler;

import com.umc.yeongkkeul.apiPayload.code.BaseErrorCode;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;

public class PurchaseHandler extends GeneralException {
    public PurchaseHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}