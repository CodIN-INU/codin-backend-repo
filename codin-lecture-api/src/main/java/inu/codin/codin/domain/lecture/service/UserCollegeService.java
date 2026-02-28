package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.repository.UserRepository;
import inu.codin.common.entity.College;
import inu.codin.security.exception.JwtException;
import inu.codin.security.exception.SecurityErrorCode;
import inu.codin.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCollegeService {

    private final UserRepository userRepository;

    public College getCurrentUserCollege() {
        String email = SecurityUtil.getUsername();

        UserEntity user = userRepository.findByEmailAndStatusAll(email)
                .orElseThrow(() -> new JwtException(SecurityErrorCode.ACCESS_DENIED, "유저를 찾을 수 없습니다."));

        if (user.getCollege() == null) {
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "유저의 단과대 정보가 없습니다.");
        }

        return user.getCollege();
    }
}
