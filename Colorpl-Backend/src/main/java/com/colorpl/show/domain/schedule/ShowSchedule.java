package com.colorpl.show.domain.schedule;

import com.colorpl.show.domain.detail.ShowDetail;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShowSchedule {

    @Column(name = "SHOW_SCHEDULE_ID")
    @GeneratedValue
    @Id
    private Integer id;

    @JoinColumn(name = "SHOW_DETAIL_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private ShowDetail showDetail;

    @Column(name = "SHOW_SCHEDULE_DATE_TIME")
    private LocalDateTime dateTime;
}