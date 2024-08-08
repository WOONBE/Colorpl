package com.colorpl.schedule.query.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.colorpl.member.Member;
import com.colorpl.member.repository.MemberRepository;
import com.colorpl.reservation.domain.Reservation;
import com.colorpl.reservation.domain.ReservationDetail;
import com.colorpl.reservation.repository.ReservationRepository;
import com.colorpl.schedule.domain.ReservationSchedule;
import com.colorpl.schedule.dto.SearchScheduleCondition;
import com.colorpl.schedule.repository.ReservationScheduleRepository;
import com.colorpl.schedule.repository.ScheduleRepository;
import com.colorpl.show.domain.detail.Category;
import com.colorpl.show.domain.detail.ShowDetail;
import com.colorpl.show.repository.ShowDetailRepository;
import com.colorpl.show.domain.schedule.ShowSchedule;
import com.colorpl.show.domain.schedule.ShowScheduleRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReservationScheduleRepositoryCustomTest {

    @Autowired
    MemberRepository memberRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationScheduleRepository reservationScheduleRepository;
    @Autowired
    ScheduleRepository scheduleRepository;
    @Autowired
    ShowDetailRepository showDetailRepository;
    @Autowired
    ShowScheduleRepository showScheduleRepository;

    @Test
    void monthlyReservationScheduleList() {

        LocalDateTime now = LocalDateTime.now();

        Member member = Member.builder()
            .email("email")
            .build();
        memberRepository.save(member);

        ShowDetail showDetail = ShowDetail.builder()
            .name("name")
            .category(Category.PLAY)
            .build();
        showDetailRepository.save(showDetail);

        ShowSchedule showSchedule = ShowSchedule.builder()
            .showDetail(showDetail)
            .dateTime(now)
            .build();
        showScheduleRepository.save(showSchedule);

        ReservationDetail reservationDetail = ReservationDetail.builder()
            .showSchedule(showSchedule)
            .row((byte) 0)
            .col((byte) 0)
            .build();
        Reservation reservation = Reservation.builder()
            .member(member)
            .build();
        reservation.addReservationDetail(reservationDetail);
        reservationRepository.save(reservation);

        ReservationSchedule createReservationSchedule = ReservationSchedule.builder()
            .reservationDetail(reservationDetail)
            .member(member)
            .build();
        scheduleRepository.save(createReservationSchedule);

        SearchScheduleCondition condition = SearchScheduleCondition.builder()
            .member(member)
            .from(now.minusDays(1))
            .to(now.plusDays(1))
            .build();

        ReservationSchedule findReservationSchedule = reservationScheduleRepository.search(
                condition)
            .get(0);

        assertThat(findReservationSchedule.getReservationDetail().getRow()).isEqualTo(
            Byte.valueOf("0"));
        assertThat(findReservationSchedule.getReservationDetail().getCol()).isEqualTo(
            Byte.valueOf("0"));
        assertThat(findReservationSchedule.getReservationDetail().getShowSchedule()
            .getDateTime()).isEqualTo(now);
        assertThat(findReservationSchedule.getReservationDetail().getShowSchedule().getShowDetail()
            .getName()).isEqualTo("name");
        assertThat(findReservationSchedule.getReservationDetail().getShowSchedule().getShowDetail()
            .getCategory()).isEqualTo(Category.PLAY);
    }
}