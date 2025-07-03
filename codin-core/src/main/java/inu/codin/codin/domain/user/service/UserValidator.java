package inu.codin.codin.domain.user.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class UserValidator {
    private final UserRepository userRepository;

    /**
     * User 존재 여부 검증
     * @param userId 존재 검증할 userId - 삭제된 유저는 검색되지 않음
     * @param exceptionSupplier Exception Class 지정
     */
    public void validateUserExists(ObjectId userId, Supplier<? extends RuntimeException> exceptionSupplier) {
        userRepository.findByUserId(userId)
                .orElseThrow(exceptionSupplier);
    }
}