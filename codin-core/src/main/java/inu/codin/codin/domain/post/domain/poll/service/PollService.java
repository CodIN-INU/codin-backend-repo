package inu.codin.codin.domain.post.domain.poll.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.poll.dto.PollCreateRequestDTO;
import inu.codin.codin.domain.post.domain.poll.dto.PollVotingRequestDTO;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.exception.PollDuplicateVoteException;
import inu.codin.codin.domain.post.domain.poll.exception.PollOptionChoiceException;
import inu.codin.codin.domain.post.domain.poll.exception.PollTimeFailException;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.post.repository.PostRepository;
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
                    return new NotFoundException("해당 게시물을 찾을 수 없습니다.");
                });

        PollEntity poll = pollRepository.findByPostId(post.get_id())
                .orElseThrow(() -> {
                    log.warn("투표 실패 - 투표 데이터 없음 - postId: {}", postId);
                    return new NotFoundException("투표 데이터가 존재하지 않습니다.");
                });

        if (LocalDateTime.now().isAfter(poll.getPollEndTime())) {
            log.warn("투표 실패 - 투표 종료됨 - pollId: {}", poll.get_id());
            throw new PollTimeFailException("이미 종료된 투표입니다.");
        }

        ObjectId userId = SecurityUtils.getCurrentUserId();
        boolean hasAlreadyVoted = pollVoteRepository.existsByPollIdAndUserId(poll.get_id(), userId);
        if (hasAlreadyVoted) {
            log.warn("투표 실패 - 중복 투표 - pollId: {}, userId: {}", poll.get_id(), userId);
            throw new PollDuplicateVoteException("이미 투표하셨습니다.");
        }

        List<Integer> selectedOptions = pollRequestDTO.getSelectedOptions();
        if (!poll.isMultipleChoice() && selectedOptions.size() > 1) {
            log.warn("투표 실패 - 복수 선택 허용 안됨 - pollId: {}, userId: {}", poll.get_id(), userId);
            throw new PollOptionChoiceException("복수 선택이 허용되지 않은 투표입니다.");
        }

        for (Integer index : selectedOptions) {
            if (index < 0 || index >= poll.getPollOptions().size()) {
                log.warn("투표 실패 - 잘못된 선택지 - pollId: {}, optionIndex: {}", poll.get_id(), index);
                throw new PollOptionChoiceException("잘못된 선택지입니다.");
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
                    return new NotFoundException("해당 게시물을 찾을 수 없습니다.");
                });

        PollEntity poll = pollRepository.findByPostId(post.get_id())
                .orElseThrow(() -> {
                    log.warn("투표 취소 실패 - 투표 데이터 없음 - postId: {}", postId);
                    return new NotFoundException("투표 데이터가 존재하지 않습니다.");
                });

        if (LocalDateTime.now().isAfter(poll.getPollEndTime())) {
            log.warn("투표 취소 실패 - 투표 종료됨 - pollId: {}", poll.get_id());
            throw new PollTimeFailException("이미 종료된 투표입니다.");
        }

        ObjectId userId = SecurityUtils.getCurrentUserId();
        PollVoteEntity pollVote = pollVoteRepository.findByPollIdAndUserId(poll.get_id(), userId)
                .orElseThrow(() -> {
                    log.warn("투표 취소 실패 - 유저 투표 내역 없음 - pollId: {}, userId: {}", poll.get_id(), userId);
                    return new NotFoundException("유저의 투표 내역이 존재하지 않습니다.");
                });

        for (Integer index : pollVote.getSelectedOptions()) {
            poll.deleteVote(index);
            log.info("투표 항목 취소 반영 - pollId: {}, optionIndex: {}", poll.get_id(), index);
        }
        pollRepository.save(poll);
        pollVoteRepository.delete(pollVote);
        log.info("투표 취소 완료 - pollId: {}, userId: {}", poll.get_id(), userId);
    }
}