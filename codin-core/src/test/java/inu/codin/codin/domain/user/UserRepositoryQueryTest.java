package inu.codin.codin.domain.user;

import inu.codin.codin.domain.user.entity.UserEntity;
import inu.codin.codin.domain.user.entity.UserRole;
import inu.codin.codin.domain.user.entity.UserStatus;
import inu.codin.codin.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
class UserRepositoryQueryTest {

    @Container
    static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry r) {
        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        // 컬렉션/DB 이름은 엔티티/설정에 따라 자동
    }

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // 매칭되어야 하는 케이스 (USER + ACTIVE + 이름 1글자)
        userRepository.save(UserEntity.builder()
                .email("user1@inu.ac.kr")
                .name("김")                       // 1글자 한글
                .nickname("u1")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        userRepository.save(UserEntity.builder()
                .email("user2@inu.ac.kr")
                .name("A")                        // 1글자 영문
                .nickname("u2")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        // 매칭되면 안 되는 케이스들
        userRepository.save(UserEntity.builder()
                .email("user3@inu.ac.kr")
                .name("홍 길동")                   // 공백 포함 (정규식 불일치)
                .nickname("u3")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        userRepository.save(UserEntity.builder()
                .email("user4@inu.ac.kr")
                .name("AB")                       // 2글자
                .nickname("u4")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build());

        userRepository.save(UserEntity.builder()
                .email("user5@inu.ac.kr")
                .name("김")                       // 1글자지만 INACTIVE
                .nickname("u5")
                .role(UserRole.USER)
                .status(UserStatus.SUSPENDED)     // 또는 INACTIVE
                .build());

        // ADMIN들
        userRepository.save(UserEntity.builder()
                .email("admin1@inu.ac.kr")
                .name("관리자")
                .nickname("a1")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());

        userRepository.save(UserEntity.builder()
                .email("admin2@inu.ac.kr")
                .name("Admin")
                .nickname("a2")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .build());
    }

    @Test
    void findActiveUsersWithOneCharName_정상동작() {
        List<UserEntity> result = userRepository.findActiveUsersWithOneCharName();
        // user1(김), user2(A)만 매칭되어야 함 -> 총 2명
        assertThat(result).extracting("email")
                .containsExactlyInAnyOrder("user1@inu.ac.kr", "user2@inu.ac.kr");
        assertThat(result).hasSize(2);
    }

    @Test
    void findActiveAdmins_정상동작() {
        List<UserEntity> result = userRepository.findActiveAdmins();
        assertThat(result).extracting("email")
                .containsExactlyInAnyOrder("admin1@inu.ac.kr", "admin2@inu.ac.kr");
        assertThat(result).hasSize(2);
    }
}