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
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByEmailAndStatusAll(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없음, email :" + email));

        if (!UserStatus.ACTIVE.equals(user.getStatus())) {
            throw new UserDisabledException("유저가 활성화되지 않았습니다, status : "+ user.getStatus());
        }

        return CustomUserDetails.from(user);
    }

}
