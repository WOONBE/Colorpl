package com.colorpl.reservation.repository;

import com.colorpl.reservation.domain.Reservation;
import com.colorpl.reservation.domain.ReservationDetail;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    //유저로 조회 추가
    List<Reservation> findByMemberId(Integer memberId);
    Optional<ReservationDetail> findDetailByIdAndMemberId(Long detailId, Integer memberId);

    List<Reservation> findByReservationDetailsShowScheduleId(Long showScheduleId);

}
