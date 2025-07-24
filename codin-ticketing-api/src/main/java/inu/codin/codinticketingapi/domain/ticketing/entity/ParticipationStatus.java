package inu.codin.codinticketingapi.domain.ticketing.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ParticipationStatus {
    COMPLETED("경품 수령 완료"),
    WAITING("경품 수령 대기"),
    CANCELED("티켓팅 취소");

    private final String description;
}
