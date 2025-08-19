package inu.codin.codin.domain.voice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.voice.entity.VoiceEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VoiceBoxDetailResponse {

    private String boxId;

    private Department department;

    private String question;

    private String answer;

    private Boolean isUserInPositive;
    private Boolean isUserInOpposite;

    private Integer userCountPositive;
    private Integer userCountOpposite;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static VoiceBoxDetailResponse of(VoiceEntity voiceEntity) {
        return VoiceBoxDetailResponse.builder()
                .boxId(voiceEntity.getId().toHexString())
                .department(voiceEntity.getDepartment())
                .question(voiceEntity.getQuestion())
                .answer(voiceEntity.getAnswer())
                .isUserInPositive(voiceEntity.getPositiveVoteIds() == null ? null : voiceEntity.getPositiveVoteIds().contains(SecurityUtils.getCurrentUserId()))
                .isUserInOpposite(voiceEntity.getOppositeVoteIds() == null ? null : voiceEntity.getOppositeVoteIds().contains(SecurityUtils.getCurrentUserId()))
                .userCountPositive(voiceEntity.getPositiveVoteIds() == null ? null : voiceEntity.getPositiveVoteIds().size())
                .userCountOpposite(voiceEntity.getOppositeVoteIds() == null ? null : voiceEntity.getOppositeVoteIds().size())
                .createdAt(voiceEntity.getCreatedAt())
                .build();
    }

}
