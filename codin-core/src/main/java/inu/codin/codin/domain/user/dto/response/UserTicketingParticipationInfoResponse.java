package inu.codin.codin.domain.user.dto.response;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.domain.user.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "유저 티켓팅 수령자 정보 반환 Dto")
public class UserTicketingParticipationInfoResponse {

    @Schema(description = "user id", example = "1111")
    private String _id;
    @Schema(description = "Enum Department 소속 정보", example = "COMPUTER_SCI")
    private Department department;
    @Schema(description = "학번", example = "202501111")
    private String studentId;
    @Schema(description = "이름", example = "홍길동")
    private String name;

    public static UserTicketingParticipationInfoResponse of(UserEntity user) {
        return UserTicketingParticipationInfoResponse.builder()
                ._id(user.get_id().toString())
                .department(user.getDepartment())
                .studentId(user.getStudentId())
                .name(user.getName())
                .build();
    }
}
