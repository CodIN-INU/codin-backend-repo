package inu.codin.codin.domain.post.domain.poll.service;

import com.mongodb.client.result.UpdateResult;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.common.util.ObjectIdUtil;
import inu.codin.codin.domain.post.domain.poll.dto.request.PollCreateRequestDTO;
import inu.codin.codin.domain.post.domain.poll.dto.request.PollVotingRequestDTO;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.exception.PollErrorCode;
import inu.codin.codin.domain.post.domain.poll.exception.PollException;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.dto.request.PostCreateRequestDTO;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.service.PostCommandService;
import inu.codin.codin.domain.post.service.PostQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollCommandService {

    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    @Transactional
    public void createPoll(PollCreateRequestDTO pollRequestDTO) {

        PostCreateRequestDTO postCreateRequestDTO = PostCreateRequestDTO.fromPoll(pollRequestDTO);
        ObjectId postId = postCommandService.createPostWithoutImagesAndReturn(postCreateRequestDTO);

        PollEntity pollEntity = PollEntity.from(postId, pollRequestDTO);
        pollRepository.save(pollEntity);

        log.info("투표 저장 완료 - postId: {}, pollId: {}", postId, pollEntity.get_id());
    }

    public void votingPoll(String postId, PollVotingRequestDTO pollRequestDTO) {
        log.info("투표 요청 - postId: {}, userId: {}", postId, SecurityUtils.getCurrentUserId());

        PollEntity poll = getActivePollByPostId(postId);
        ObjectId userId = SecurityUtils.getCurrentUserId();

        ensureNotDuplicatedVote(poll.get_id(), userId);

        List<Integer> selectedOptions = pollRequestDTO.getSelectedOptions();
        // 단일,복수 선택 규칙 + 인덱스 범위 검증
        validateSelections(poll, selectedOptions);

        PollVoteEntity vote = PollVoteEntity.from(poll.get_id(), userId, selectedOptions);
        pollVoteRepository.save(vote);
        log.info("투표 기록 저장 완료 - pollId: {}, userId: {}", poll.get_id(), userId);

        // 카운트 반영 (원자적 증감)
        incVote(poll, selectedOptions);
        log.info("투표 완료 - pollId: {}, userId: {}", poll.get_id(), userId);
    }

    public void deleteVoting(String postId) {
        log.info("투표 취소 요청 - postId: {}, userId: {}", postId, SecurityUtils.getCurrentUserId());

        PollEntity poll = getActivePollByPostId(postId);
        ObjectId userId = SecurityUtils.getCurrentUserId();

        PollVoteEntity vote = requireUserVote(poll.get_id(), userId);

        // 카운트 롤백 (원자적 증감)
        dcrVote(poll, vote.getSelectedOptions());
        pollVoteRepository.delete(vote);
        log.info("투표 취소 완료 - pollId: {}, userId: {}", poll.get_id(), userId);
    }



    // ---- 비즈니스 규칙 검증 / 조회 컨텍스트 ----
    private PollEntity findPollByPostId(ObjectId postId) {
        return pollRepository.findByPostId(postId)
                .orElseThrow(() -> new PollException(PollErrorCode.POLL_NOT_FOUND));
    }

    private PollEntity getActivePollByPostId(String postId) {
        PostEntity post = postQueryService.findPostById(ObjectIdUtil.toObjectId(postId));
        PollEntity poll = findPollByPostId(post.get_id());
        validatePollActive(poll);
        return poll;
    }

    private void validatePollActive(PollEntity poll) {
        if (LocalDateTime.now().isAfter(poll.getPollEndTime())) {
            log.warn("투표 종료됨 - pollId: {}", poll.get_id());
            throw new PollException(PollErrorCode.POLL_FINISHED);
        }
    }

    private void validateSelections(PollEntity poll, List<Integer> selected) {
        if (!poll.isMultipleChoice() && selected.size() > 1) {
            log.warn("복수 선택 허용 안됨 - pollId: {}", poll.get_id());
            throw new PollException(PollErrorCode.MULTIPLE_CHOICE_NOT_ALLOWED);
        }
        int size = poll.getPollOptions().size();
        for (Integer idx : selected) {
            if (idx == null || idx < 0 || idx >= size) {
                log.warn("잘못된 선택지 - pollId: {}, optionIndex: {}", poll.get_id(), idx);
                throw new PollException(PollErrorCode.INVALID_OPTION);
            }
        }
    }

    private void ensureNotDuplicatedVote(ObjectId pollId, ObjectId userId) {
        if (pollVoteRepository.existsByPollIdAndUserId(pollId, userId)) {
            log.warn("중복 투표 - pollId: {}, userId: {}", pollId, userId);
            throw new PollException(PollErrorCode.POLL_DUPLICATED);
        }
    }

    private PollVoteEntity requireUserVote(ObjectId pollId, ObjectId userId) {
        return pollVoteRepository.findByPollIdAndUserId(pollId, userId)
                .orElseThrow(() -> {
                    log.warn("투표 내역 없음 - pollId: {}, userId: {}", pollId, userId);
                    return new PollException(PollErrorCode.POLL_VOTE_USER_NOT_FOUND);
                });
    }

    // ---- 투표 수 카운트 갱신 ----
    private void incVote(PollEntity poll, List<Integer> selected) {
        final int size = poll.getPollOptions().size();
        selected.forEach(idx -> {
            validateIndex(size, idx);
            long result = pollRepository.incOption(poll.get_id(), idx);
            if (result == 0) {
                log.warn("투표 증가 실패 - pollId: {}, optionIndex: {}", poll.get_id(), idx);
                throw new PollException(PollErrorCode.POLL_VOTE_STATE_CONFLICT);
            }
        });
    }

    private void dcrVote(PollEntity poll, List<Integer> selected) {
        final int size = poll.getPollOptions().size();
        selected.forEach(idx -> {
            validateIndex(size, idx);
            long result = pollRepository.dcrOptionIfPositive(poll.get_id(), idx);
            if (result == 0) {
                log.warn("투표 감소 실패 - pollId: {}, optionIndex: {}", poll.get_id(), idx);
                throw new PollException(PollErrorCode.POLL_VOTE_STATE_CONFLICT);
            }
        });
    }

    private void validateIndex(int size, Integer idx) {
        if (idx == null || idx < 0 || idx >= size) {
            throw new PollException(PollErrorCode.INVALID_OPTION);
        }
    }

}
