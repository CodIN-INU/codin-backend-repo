package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.lecture.entity.Emotion;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final EmotionRepository emotionRepository;

    @Transactional
    public Emotion getOrMakeEmotion(Lecture lecture) {
        Emotion emotion = lecture.getEmotion();
        if (emotion == null) {
            emotion = emotionRepository.save(new Emotion(lecture));
            lecture.assignEmotion(emotion);
        }
        return emotion;
    }
}
