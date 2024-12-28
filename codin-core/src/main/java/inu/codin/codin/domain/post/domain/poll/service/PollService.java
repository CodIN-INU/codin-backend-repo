package inu.codin.codin.domain.post.domain.poll.service;

import inu.codin.codin.common.exception.NotFoundException;
import inu.codin.codin.common.security.exception.JwtException;
import inu.codin.codin.common.security.exception.SecurityErrorCode;
import inu.codin.codin.common.security.util.SecurityUtils;
import inu.codin.codin.domain.post.domain.poll.dto.PollCreateRequestDTO;
import inu.codin.codin.domain.post.domain.poll.dto.PollVotingRequestDTO;
import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;

import inu.codin.codin.domain.post.domain.poll.exception.PollOptionChoiceException;
import inu.codin.codin.domain.post.domain.poll.exception.PollTimeFailException;
import inu.codin.codin.domain.post.entity.PostEntity;
import inu.codin.codin.domain.post.entity.PostStatus;
import inu.codin.codin.domain.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import inu.codin.codin.domain.post.repository.PostRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PollService {

    private final PostRepository postRepository;
    private final PollRepository pollRepository;

    @Transactional
    public void createPoll(PollCreateRequestDTO pollRequestDTO) {

        ObjectId userId = SecurityUtils.getCurrentUserId();
        //권한 확인 처리 로직 추가
        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                pollRequestDTO.getPostCategory().toString().split("_")[0].equals("POLL")){
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "투표에 대한 권한이 없습니다.");
        }

        // 1. PostEntity 생성 및 저장
        PostEntity postEntity = PostEntity.builder()
                .title(pollRequestDTO.getTitle())
                .content(pollRequestDTO.getContent())
                .userId(userId)
                .postCategory(pollRequestDTO.getPostCategory())
                .isAnonymous(pollRequestDTO.isAnonymous())
                .postStatus(PostStatus.ACTIVE)
                .build();
        postEntity = postRepository.save(postEntity);

        // 2. PollEntity 생성 및 저장
        PollEntity pollEntity = PollEntity.builder()
                .postId(postEntity.get_id())
                .pollOptions(pollRequestDTO.getPollOptions())
                .pollEndTime(pollRequestDTO.getPollEndTime())
                .multipleChoice(pollRequestDTO.isMultipleChoice())
                .build();
        pollRepository.save(pollEntity);
    }

    public void votingPoll(String postId, PollVotingRequestDTO pollRequestDTO) {
        // 1. 게시글 조회 및 검증
        PostEntity post = postRepository.findByIdAndNotDeleted(new ObjectId(postId))
                .orElseThrow(() -> new NotFoundException("해당 게시물을 찾을 수 없습니다."));
        validateUserAndPost(post);

        // 2. 투표 데이터(PollEntity) 조회
        PollEntity poll = pollRepository.findByPostId(post.get_id())
                .orElseThrow(() -> new NotFoundException("투표 데이터가 존재하지 않습니다."));

        // 3. 투표 종료 여부 확인
        if (LocalDateTime.now().isAfter(poll.getPollEndTime())) {
            throw new PollTimeFailException("이미 종료된 투표입니다.");
        }

        // 4. 사용자의 선택 항목 검증
        List<Integer> selectedOptions = pollRequestDTO.getSelectedOptions();
        if (!poll.isMultipleChoice() && selectedOptions.size() > 1) {
            throw new PollOptionChoiceException("복수 선택이 허용되지 않은 투표입니다.");
        }
        for (Integer index : selectedOptions) {
            if (index < 0 || index >= poll.getPollOptions().size()) {
                throw new PollOptionChoiceException("잘못된 선택지입니다.");
            }
        }

        // 5. 투표 항목 DB 반영
        for (Integer index : selectedOptions) {
            poll.vote(index);
        }
        pollRepository.save(poll);
    }

    private void validateUserAndPost(PostEntity post) {
        if (SecurityUtils.getCurrentUserRole().equals(UserRole.USER) &&
                post.getPostCategory().toString().split("_")[0].equals("POLL")){
            throw new JwtException(SecurityErrorCode.ACCESS_DENIED, "투표 게시글에 대한 권한이 없습니다.");
        }
        SecurityUtils.validateUser(post.getUserId());
    }
}