package inu.codin.codin.domain.post.domain.poll.service;

import inu.codin.codin.domain.post.domain.poll.entity.PollEntity;
import inu.codin.codin.domain.post.domain.poll.entity.PollVoteEntity;
import inu.codin.codin.domain.post.domain.poll.exception.PollErrorCode;
import inu.codin.codin.domain.post.domain.poll.exception.PollException;
import inu.codin.codin.domain.post.domain.poll.repository.PollRepository;
import inu.codin.codin.domain.post.domain.poll.repository.PollVoteRepository;
import inu.codin.codin.domain.post.dto.response.PollInfoResponseDTO;
import inu.codin.codin.domain.post.entity.PostEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollQueryService {

    private final PollRepository pollRepository;
    private final PollVoteRepository pollVoteRepository;

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
