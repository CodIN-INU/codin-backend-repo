package inu.codin.lecture.domain.lecture.service;

import inu.codin.codin.domain.user.service.UserCollegeReader;
import inu.codin.common.entity.College;
import inu.codin.security.exception.JwtException;
import inu.codin.security.exception.SecurityErrorCode;
import inu.codin.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCollegeService {

    private final UserCollegeReader userCollegeReader;

    public College getCurrentUserCollege() {
        String email = SecurityUtil.getUsername();
        College college = userCollegeReader.findCollegeByEmail(email);

        if (college == null) {
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "유저의 단과대 정보가 없습니다.");
        }

        return college;
    }
}
