package com.colorpl.review.domain;

import com.colorpl.comment.domain.Comment;
import com.colorpl.global.common.BaseEntity;
import com.colorpl.schedule.domain.Schedule;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Getter
//@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    @Column(name = "REVIEW_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "REVIEW_FILENAME")
    private String filename;

    @Column(name = "REVIEW_CONTENT")
    private String content;

    @Column(name = "IS_SPOILER")
    private Boolean spoiler;

    @Column(name = "REVIEW_EMOTION")
    private Byte emotion;

    @Column(name = "EMPHATHY_NUMBER")
    @Builder.Default
    private Integer emphathy = 0;

    @OneToMany(mappedBy = "review", cascade = {CascadeType.REMOVE})
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_ID", referencedColumnName = "SCHEDULE_ID")
    private Schedule schedule;

    // 공감 유저 추적
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Empathy> empathies = new ArrayList<>();

    public void updateReview(String content, Boolean spoiler, Byte emotion) {
        if (content != null) {
            this.content = content;
        }
        if (spoiler != null) {
            this.spoiler = spoiler;
        }
        if (emotion != null) {
            this.emotion = emotion;
        }
    }

    // 공감 수 증가
    public void incrementEmphathy() {
        this.emphathy = (this.emphathy == null) ? 1 : this.emphathy + 1;
    }

    // 공감 수 감소
    public void decrementEmphathy() {
        if (this.emphathy != null && this.emphathy > 0) {
            this.emphathy = this.emphathy - 1;
        }
    }
}
