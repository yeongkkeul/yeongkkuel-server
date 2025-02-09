package com.umc.yeongkkeul.domain.enums;

import lombok.Getter;

@Getter
public enum AgeGroup {
    UNDECIDED("미선택"),
    TEENAGER("10대"),
    TWENTIES("20대"),
    THIRTIES("30대"),
    FORTIES("40대"),
    FIFTIES("50대"),
    SIXTIES_AND_ABOVE("60대 이상");

    private final String ageGroup;

    AgeGroup(String ageGroup) {
        this.ageGroup = ageGroup;
    }
}