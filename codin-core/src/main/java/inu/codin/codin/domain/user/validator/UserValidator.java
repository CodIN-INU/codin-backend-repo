package inu.codin.codin.domain.user.validator;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private UserRepository userRepository;

    /**
     * User 존재 여부 검증
     * @param userId 존재 검증할 userId - 삭제된 유저는 검색되지 않음
     * @param exceptionMsg Exception 메세지
     */
    public void validateUserExists(ObjectId userId, String exceptionMsg) {
        userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(exceptionMsg));
    }
}