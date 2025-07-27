package inu.codin.codin.domain.lecture.service;

import inu.codin.codin.domain.lecture.entity.Emotion;
import inu.codin.codin.domain.lecture.entity.Lecture;
import inu.codin.codin.domain.lecture.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final EmotionRepository emotionRepository;

    public Emotion getOrMakeEmotion(Lecture lecture) {
        if (lecture.getEmotion() == null) {
            Emotion emotion = emotionRepository.save(new Emotion(lecture));
            lecture.setEmotion(emotion);
            return emotion;
        }
        return lecture.getEmotion();
    }
}
