package inu.codin.lecture.domain.lecture.repository;

import inu.codin.lecture.domain.lecture.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmotionRepository extends JpaRepository<Emotion, Long> {
}
