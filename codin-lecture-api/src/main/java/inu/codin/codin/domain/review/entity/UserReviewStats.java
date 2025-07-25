package inu.codin.codin.domain.review.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserReviewStats {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private int countOfReviews = 0;
    private boolean openKeyword = false;

    public UserReviewStats(String userId){
        this.userId = userId;
    }

    public void increaseCount(){
        this.countOfReviews++;
    }

    public void canOpenKeyword(){
        this.openKeyword = true;
    }
}
