package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.exception.LectureErrorCode;
import inu.codin.codin.domain.lecture.exception.LectureException;
import inu.codin.codin.domain.lecture.repository.LectureRepository;
import inu.codin.codin.domain.review.entity.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.openai.OpenAiChatModel;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectureSummarizationService {

    private final OpenAiChatModel chatModel;
    private final LectureRepository lectureRepository;

    @Transactional
    public void summarizeLecture(Long lectureId) {
        Lecture lecture = lectureRepository.findLectureWithTagsAndReviewsById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorCode.LECTURE_NOT_FOUND));

        // 리뷰들을 하나의 문자열화
        String reviewsText = lecture.getReviews().stream()
                .map(Review::getContent)
                .collect(Collectors.joining("\n"));

        // 강의 태그 메타데이터 문자열화
        String tags = lecture.getTags().stream()
                .map(t -> t.getTag().getTagName())
                .collect(Collectors.joining(", "));

        // Prompt 생성
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("당신은 친절한 대학 강의 큐레이션 도우미입니다."),
                        new UserMessage(buildPrompt(lecture, reviewsText, tags))
                )
        );

        // ChatModel 호출 -> AI 요약문 반환
        ChatResponse response = chatModel.call(prompt);
        String aiSummary = response.getResult().getOutput().getText();

        // AI 요약문 저장
        lecture.updateAiSummary(aiSummary);
    }

    private String buildPrompt(Lecture lecture, String reviews, String tags) {
        return """
            아래 정보를 토대로 “질문: 답변” 형태로 총 700자 이내로 요약해 주세요.

            【교과목명】%s
            【학년】%d학년
            【학점】%d학점
            【교수】%s
            【학과】%s
            【수업 유형】%s
            【수업 방식】%s
            【평가 방식】%s
            【태그】%s
            【강의 계획서】%s

            【수강 후기】
            %s

            1. 이 수업, 뭘 배우나요?
            2. 수업 난이도는?
            3. 누가 들으면 좋을까요?
            4. 수업 방식은?
            5. 내가 얻을 수 있는 것은?
            """
                .formatted(
                        lecture.getLectureNm(),
                        lecture.getGrade(),
                        lecture.getCredit(),
                        lecture.getProfessor(),
                        lecture.getDepartment(),
                        lecture.getType(),
                        lecture.getLectureType(),
                        lecture.getEvaluation(),
                        tags.isEmpty() ? "태그 없음" : tags,
                        lecture.getSyllabus(),
                        reviews
                );
    }
}
