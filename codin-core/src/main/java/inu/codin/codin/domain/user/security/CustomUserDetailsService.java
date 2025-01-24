package inu.codin.codin.domain.user.security;

import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserStatus;
import inu.codin.codin.domain.user.exception.UserDisabledException;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String studentId) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByStudentId(studentId)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없음, studentId :" + studentId));

        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new UserDisabledException("유저가 활성화되지 않았습니다");
        }

        return CustomUserDetails.from(user);
    }

}
