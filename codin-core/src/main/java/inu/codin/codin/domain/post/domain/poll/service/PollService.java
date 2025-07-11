package inu.codin.codin.domain.post.domain.poll.service;

import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.poll.dto.PollCreateRequestDTO;
import inu.codin.codin.domain.post.domain.poll.dto.PollVotingRequestDTO;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.exception.PollException;
import inu.codin.codin.domain.post.domain.poll.exception.PollErrorCode;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.dto.response.PollInfoResponseDTO;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class PollService {

    private final PostRepository postRepository;
    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;

    @Transactional
    public void createPoll(PollCreateRequestDTO pollRequestDTO) {
        log.info("투표 생성 요청 - title: {}, userId: {}", pollRequestDTO.getTitle(), SecurityUtils.getCurrentUserId());

        ObjectId userId = SecurityUtils.getCurrentUserId();

        // PostEntity 생성 및 저장
        PostEntity postEntity = PostEntity.builder()
                .title(pollRequestDTO.getTitle())
                .content(pollRequestDTO.getContent())
                .userId(userId)
                .postCategory(pollRequestDTO.getPostCategory())
                .isAnonymous(pollRequestDTO.isAnonymous())
                .postStatus(PostStatus.ACTIVE)
                .build();
        postEntity = postRepository.save(postEntity);
        log.info("게시글 저장 완료 - postId: {}", postEntity.get_id());

        // PollEntity 생성 및 저장
        PollEntity pollEntity = PollEntity.builder()
                .postId(postEntity.get_id())
                .pollOptions(pollRequestDTO.getPollOptions())
                .pollEndTime(pollRequestDTO.getPollEndTime())
                .multipleChoice(pollRequestDTO.isMultipleChoice())
                .build();
        pollRepository.save(pollEntity);
        log.info("투표 저장 완료 - pollId: {}", pollEntity.get_id());
    }

    public void votingPoll(String postId, PollVotingRequestDTO pollRequestDTO) {
        log.info("투표 요청 - postId: {}, userId: {}", postId, SecurityUtils.getCurrentUserId());

        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> {
                    log.warn("투표 실패 - 게시글 없음 - postId: {}", postId);
                    return new PollException(PollErrorCode.POST_NOT_FOUND);
                });

        PollEntity poll = pollRepository.findByPostId(post.get_id())
                .orElseThrow(() -> {
                    log.warn("투표 실패 - 투표 데이터 없음 - postId: {}", postId);
                    return new PollException(PollErrorCode.POLL_NOT_FOUND);
                });

        if (LocalDateTime.now().isAfter(poll.getPollEndTime())) {
            log.warn("투표 실패 - 투표 종료됨 - pollId: {}", poll.get_id());
            throw new PollException(PollErrorCode.POLL_FINISHED);
        }

        ObjectId userId = SecurityUtils.getCurrentUserId();
        boolean hasAlreadyVoted = pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId);
        if (hasAlreadyVoted) {
            log.warn("투표 실패 - 중복 투표 - pollId: {}, userId: {}", poll.get_id(), userId);
            throw new PollException(PollErrorCode.POLL_DUPLICATED);
        }

        List<Integer> selectedOptions = pollRequestDTO.getSelectedOptions();
        if (!poll.isMultipleChoice() && selectedOptions.size() > 1) {
            log.warn("투표 실패 - 복수 선택 허용 안됨 - pollId: {}, userId: {}", poll.get_id(), userId);
            throw new PollException(PollErrorCode.MULTIPLE_CHOICE_NOT_ALLOWED);
        }

        for (Integer index : selectedOptions) {
            if (index < 0 || index >= poll.getPollOptions().size()) {
                log.warn("투표 실패 - 잘못된 선택지 - pollId: {}, optionIndex: {}", poll.get_id(), index);
                throw new PollException(PollErrorCode.INVALID_OPTION);
            }
        }

        PollVoteEntity vote = PollVoteEntity.builder()
                .pollId(poll.get_id())
                .userId(userId)
                .selectedOptions(selectedOptions)
                .build();
        pollVoteRepository.save(vote);
        log.info("투표 기록 저장 완료 - pollId: {}, userId: {}", poll.get_id(), userId);

        for (Integer index : selectedOptions) {
            poll.vote(index);
            log.info("투표 항목 반영 - pollId: {}, optionIndex: {}", poll.get_id(), index);
        }
        pollRepository.save(poll);
        log.info("투표 완료 - pollId: {}, userId: {}", poll.get_id(), userId);
    }

    public void deleteVoting(String postId) {
        log.info("투표 취소 요청 - postId: {}, userId: {}", postId, SecurityUtils.getCurrentUserId());

        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> {
                    log.warn("투표 취소 실패 - 게시글 없음 - postId: {}", postId);
                    return new PollException(PollErrorCode.POST_NOT_FOUND);
                });

        PollEntity poll = pollRepository.findByPostId(post.get_id())
                .orElseThrow(() -> {
                    log.warn("투표 취소 실패 - 투표 데이터 없음 - postId: {}", postId);
                    return new PollException(PollErrorCode.POLL_NOT_FOUND);
                });

        if (LocalDateTime.now().isAfter(poll.getPollEndTime())) {
            log.warn("투표 취소 실패 - 투표 종료됨 - pollId: {}", poll.get_id());
            throw new PollException(PollErrorCode.POLL_FINISHED);
        }

        ObjectId userId = SecurityUtils.getCurrentUserId();
        PollVoteEntity pollVote = pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)
                .orElseThrow(() -> {
                    log.warn("투표 취소 실패 - 유저 투표 내역 없음 - pollId: {}, userId: {}", poll.get_id(), userId);
                    return new PollException(PollErrorCode.POLL_NOT_FOUND);
                });

        for (Integer index : pollVote.getSelectedOptions()) {
            poll.deleteVote(index);
            log.info("투표 항목 취소 반영 - pollId: {}, optionIndex: {}", poll.get_id(), index);
        }
        pollRepository.save(poll);
        pollVoteRepository.delete(pollVote);
        log.info("투표 취소 완료 - pollId: {}, userId: {}", poll.get_id(), userId);
    }

    public PollInfoResponseDTO getPollInfo(PostEntity post, ObjectId userId) {
        PollEntity poll = pollRepository.findByPostId(post.get_id())
                .orElseThrow(() -> new PollException(PollErrorCode.POLL_NOT_FOUND));
        long totalParticipants = pollVoteRepository.countByPollId(poll.get_id());
        List<Integer> userVotes = pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)
                .map(PollVoteEntity::getSelectedOptions)
                .orElse(Collections.emptyList());
        boolean pollFinished = poll.getPollEndTime() != null && LocalDateTime.now().isAfter(poll.getPollEndTime());
        boolean hasUserVoted = pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId);
        return PollInfoResponseDTO.of(
                poll.getPollOptions(), poll.getPollEndTime(), poll.isMultipleChoice(),
                poll.getPollVotesCounts(), userVotes, totalParticipants, hasUserVoted, pollFinished);
    }
}