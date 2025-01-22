package com.umc.yeongkkeul.apiPayload.exception.handler;

import com.umc.yeongkkeul.apiPayload.code.BaseErrorCode;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;

public class ExpenseHandler extends GeneralException {
    public ExpenseHandler(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
