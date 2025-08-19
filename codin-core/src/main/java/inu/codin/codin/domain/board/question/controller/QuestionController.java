package inu.codin.codin.domain.board.question.controller;

import inu.codin.codin.common.dto.Department;
import inu.codin.codin.common.response.ListResponse;
import inu.codin.codin.common.response.SingleResponse;
import inu.codin.codin.domain.board.question.dto.request.QuestionCreateUpdateRequestDto;
import inu.codin.codin.domain.board.question.dto.response.QuestionResponseDto;
import inu.codin.codin.domain.board.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
@Tag(name = "Question API", description = "[리다지인] 게시판 자주 묻는 질문 API")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /*
    ===================
    ROLE_USER 사용한 API (조회)
    ===================
     */

    @Operation(
            summary = "자주 묻는 질문 조회"
    )
    @GetMapping
    public ResponseEntity<ListResponse<QuestionResponseDto>> getAllQuestions(@RequestParam(value = "department") Department department) {
        List<QuestionResponseDto> questions = questionService.getAllQuestions(department);
        return ResponseEntity.ok()
                .body(new ListResponse<>(200, "자주 묻는 질문 조회 성공", questions));
    }

    /*
    ===================
    ROLE_MANAGER, ROLE_ADMIN 사용한 API (작성, 수정, 삭제)
    ===================
     */

    @Operation(
            summary = "자주 묻는 질문 작성"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<SingleResponse<?>> createQuestion(@RequestBody QuestionCreateUpdateRequestDto requestDto) {
        questionService.createQuestion(requestDto);
        return ResponseEntity.status(201)
                .body(new SingleResponse<>(201, "자주 묻는 질문 작성 성공", null));
    }


    @Operation(
            summary = "자주 묻는 질문 수정"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @PutMapping("/{questionId}")
    public ResponseEntity<SingleResponse<?>> updateQuestion(@PathVariable("questionId") String id,
                                                            @RequestBody QuestionCreateUpdateRequestDto requestDto) {
        questionService.updateQuestion(id, requestDto);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "자주 묻는 질문 수정 성공", null));
    }

    @Operation(
            summary = "자주 묻는 질문 삭제"
    )
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @DeleteMapping("/{questionId}")
    public ResponseEntity<SingleResponse<?>> deleteQuestion(@PathVariable("questionId") String id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok()
                .body(new SingleResponse<>(200, "자주 묻는 질문 삭제 성공", null));
    }

}
