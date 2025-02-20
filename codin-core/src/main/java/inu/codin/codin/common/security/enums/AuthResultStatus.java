package inu.codin.codin.common.security.enums;

public enum AuthResultStatus {
    LOGIN_SUCCESS,          // 기존 회원이며, 프로필이 완료되어 정상 로그인
    NEW_USER_REGISTERED,    // 신규 회원 등록 완료 (프로필 설정 미완료)
    PROFILE_INCOMPLETE      // 기존 회원이지만 프로필 설정이 미완료됨
}
