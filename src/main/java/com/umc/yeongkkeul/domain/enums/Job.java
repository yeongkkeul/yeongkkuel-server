package com.umc.yeongkkeul.domain.enums;

import lombok.Getter;

@Getter
public enum Job {
    STUDENT("학생"),
    EMPLOYEE("직장인"),
    HOMEMAKER("주부"),
    SELF_EMPLOYED("자영업자"),
    UNDECIDED("미선택");

    private final String job;

    Job(String job) {
       this.job = job;
    }
}
