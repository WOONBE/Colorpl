package com.colorpl.reservation.service;

import com.colorpl.global.common.exception.MemberMismatchException;
import com.colorpl.global.common.exception.MemberNotFoundException;
import com.colorpl.global.common.exception.ReservationDetailNotFoundException;
import com.colorpl.global.common.exception.ReservationNotFoundException;
import com.colorpl.global.common.exception.ShowScheduleNotFoundException;
import com.colorpl.member.Member;
import com.colorpl.member.repository.MemberRepository;
import com.colorpl.reservation.domain.Reservation;
import com.colorpl.reservation.domain.ReservationDetail;
import com.colorpl.reservation.dto.ReservationDTO;
import com.colorpl.reservation.dto.ReservationDetailDTO;
import com.colorpl.reservation.repository.ReservationDetailRepository;
import com.colorpl.reservation.repository.ReservationRepository;
import com.colorpl.reservation.status.service.DisableReservationService;
import com.colorpl.reservation.status.service.EnableReservationService;
import com.colorpl.show.domain.ShowSchedule;
import com.colorpl.show.repository.ShowScheduleRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ShowScheduleRepository showScheduleRepository;
    private final ReservationDetailRepository reservationDetailRepository;
    private final EnableReservationService enableReservationService;
    private final DisableReservationService disableReservationService;

    @Transactional(readOnly = true)
    public List<ReservationDTO> getReservationsByMemberId(Integer memberId) {
        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException();
        }

        return reservations.stream()
            .map(ReservationDTO::toReservationDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationDTO getReservationByMemberIdAndReservationId(Integer memberId,
        Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .filter(res -> res.getMember().getId().equals(memberId))
            .orElseThrow(ReservationNotFoundException::new);

        return ReservationDTO.toReservationDTO(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationDTO> getAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll();

        if (reservations.isEmpty()) {
            throw new ReservationNotFoundException();
        }

        return reservations.stream()
            .map(ReservationDTO::toReservationDTO)
            .collect(Collectors.toList());
    }

    // 모든 예매 삭제
    @Transactional
    public void deleteAllReservationsByMemberId(Integer memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        List<Reservation> reservations = reservationRepository.findByMemberId(memberId);

        // 예매상세에 변경사항 반영
        reservations.forEach(
            reservation -> reservation.getReservationDetails()
                .forEach(reservationDetail -> enableReservationService.enableReservation(
                    reservationDetail.getShowSchedule().getId(),
                    Integer.valueOf(reservationDetail.getRow()),
                    Integer.valueOf(reservationDetail.getCol())
                )));

        // 각 예약을 member에서 제거
        reservations.forEach(member::removeReservation);

        // 모든 예약 삭제
        reservationRepository.deleteAll(reservations);
    }

    //예매 취소
    @Transactional
    public void cancelReservationById(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(ReservationNotFoundException::new);

        reservation.updateRefundState(true);  // is_refunded 필드를 true로 설정
        reservationRepository.save(reservation);  // 변경 사항을 저장

        // 예매상세에 변경사항 반영
        reservation.getReservationDetails()
            .forEach(reservationDetail -> enableReservationService.enableReservation(
                reservationDetail.getShowSchedule().getId(),
                Integer.valueOf(reservationDetail.getRow()),
                Integer.valueOf(reservationDetail.getCol())
            ));
    }

    //특정 멤버의 특정 예약을 취소
    @Transactional
    public void cancelReservationByMemberIdAndReservationId(Integer memberId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .filter(res -> res.getMember().getId().equals(memberId))
            .orElseThrow(MemberMismatchException::new);

        reservation.updateRefundState(true);  // is_refunded 필드를 true로 설정
        reservationRepository.save(reservation);

        // 예매상세에 변경사항 반영
        reservation.getReservationDetails()
            .forEach(reservationDetail -> enableReservationService.enableReservation(
                reservationDetail.getShowSchedule().getId(),
                Integer.valueOf(reservationDetail.getRow()),
                Integer.valueOf(reservationDetail.getCol())
            ));
    }

    //    @Transactional
//    public ReservationDTO updateReservation(Integer memberId, Long reservationId,
//        ReservationDTO reservationDTO) {
//        // 예약을 가져오고 멤버 ID를 확인
//        Reservation reservation = reservationRepository.findById(reservationId)
//            .filter(res -> res.getMember().getId().equals(memberId))
//            .orElseThrow(MemberMismatchException::new);
//
//        // 기존 예약 정보를 업데이트
//        reservation.updateReservation(reservationDTO.getDate(), reservationDTO.getAmount(),
//            reservationDTO.getComment(), reservationDTO.isRefunded());
//
//        // 새로 추가된 예약 상세 정보를 저장
//        Set<Long> newReservationDetailIds = reservationDTO.getReservationDetails().stream()
//            .map(ReservationDetailDTO::getId) // ID를 추출
//            .filter(Objects::nonNull) // ID가 null이 아닌 것만 필터링
//            .collect(Collectors.toSet());
//
//        // 기존 예약 상세 항목을 안전하게 수정하기 위해 컬렉션을 복사
//        List<ReservationDetail> existingDetails = new ArrayList<>(
//            reservation.getReservationDetails());
//
//        // 기존 예약 상세 항목을 업데이트하거나 삭제
//        existingDetails.forEach(existingDetail -> {
//            if (newReservationDetailIds.contains(existingDetail.getId())) {
//                // 새로 받은 DTO로 업데이트
//                ReservationDetailDTO detailDTO = reservationDTO.getReservationDetails().stream()
//                    .filter(dto -> dto.getId().equals(existingDetail.getId()))
//                    .findFirst()
//                    .orElseThrow(ReservationDetailNotFoundException::new);
//
//                ShowSchedule showSchedule = showScheduleRepository.findById(
//                        detailDTO.getShowScheduleId())
//                    .orElseThrow(ShowScheduleNotFoundException::new);
//
//                existingDetail.updateDetail(detailDTO.getRow(), detailDTO.getCol(), showSchedule);
//            } else {
//                // 기존 예약 상세가 새 데이터에 없으면 삭제
//                reservation.removeReservationDetail(existingDetail);
//            }
//        });
//
//        // 새로운 예약 상세 항목 추가
//        List<ReservationDetail> newReservationDetails = reservationDTO.getReservationDetails()
//            .stream()
//            .filter(detailDTO -> detailDTO.getId() == null) // ID가 없는 항목은 새 항목
//            .map(detailDTO -> {
//                ShowSchedule showSchedule = showScheduleRepository.findById(
//                        detailDTO.getShowScheduleId())
//                    .orElseThrow(ShowScheduleNotFoundException::new);
//
//                return ReservationDetail.builder()
//                    .row(detailDTO.getRow())
//                    .col(detailDTO.getCol())
//                    .showSchedule(showSchedule)
//                    .reservation(reservation) // 여기서 `reservation` 설정
//                    .build();
//            })
//            .toList();
//
//        newReservationDetails.forEach(reservation::addReservationDetail);
//
//        // 예약 저장 및 DTO 변환
//        Reservation updatedReservation = reservationRepository.save(reservation);
//
//        // 변경된 예약 반환
//        return ReservationDTO.toReservationDTO(updatedReservation);
//    }
    @Transactional
    public ReservationDTO updateReservation(Integer memberId, Long reservationId,
        ReservationDTO reservationDTO) {
        // 예약을 가져오고 멤버 ID를 확인
        Reservation reservation = reservationRepository.findById(reservationId)
            .filter(res -> res.getMember().getId().equals(memberId))
            .orElseThrow(MemberMismatchException::new);

        // 기존 예약 정보를 업데이트
        reservation.updateReservation(reservationDTO.getDate(), reservationDTO.getAmount(),
            reservationDTO.getComment(), reservationDTO.isRefunded());

        // 새로 추가된 예약 상세 정보를 저장하기 위한 ID 목록
        Set<Long> newReservationDetailIds = reservationDTO.getReservationDetails().stream()
            .map(ReservationDetailDTO::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // 기존 예약 상세 항목을 업데이트하거나 삭제
        List<ReservationDetail> existingDetails = new ArrayList<>(
            reservation.getReservationDetails());
        existingDetails.forEach(existingDetail -> {
            if (newReservationDetailIds.contains(existingDetail.getId())) {
                // 새로 받은 DTO로 업데이트
                ReservationDetailDTO detailDTO = reservationDTO.getReservationDetails().stream()
                    .filter(dto -> dto.getId().equals(existingDetail.getId()))
                    .findFirst()
                    .orElseThrow(ReservationDetailNotFoundException::new);

                ShowSchedule showSchedule = showScheduleRepository.findById(
                        detailDTO.getShowScheduleId())
                    .orElseThrow(ShowScheduleNotFoundException::new);

                existingDetail.updateDetail(detailDTO.getRow(), detailDTO.getCol(), showSchedule);
                reservationDetailRepository.save(existingDetail); // 업데이트된 정보 저장
            } else {
                // 기존 예약 상세가 새 데이터에 없으면 삭제
                reservation.removeReservationDetail(existingDetail);
                reservationDetailRepository.delete(existingDetail); // 삭제된 정보 반영
            }
        });

        // 새로운 예약 상세 항목 추가
        List<ReservationDetail> newReservationDetails = reservationDTO.getReservationDetails()
            .stream()
            .filter(detailDTO -> detailDTO.getId() == null) // ID가 없는 항목은 새 항목
            .map(detailDTO -> {
                ShowSchedule showSchedule = showScheduleRepository.findById(
                        detailDTO.getShowScheduleId())
                    .orElseThrow(ShowScheduleNotFoundException::new);

                ReservationDetail newDetail = ReservationDetail.builder()
                    .row(detailDTO.getRow())
                    .col(detailDTO.getCol())
                    .showSchedule(showSchedule)
                    .reservation(reservation)
                    .build();

                reservation.addReservationDetail(newDetail);
                return newDetail;
            })
            .toList();

        reservationDetailRepository.saveAll(newReservationDetails); // 새로운 예약 상세 정보 저장

        // 예약 저장 및 DTO 변환
        Reservation updatedReservation = reservationRepository.save(reservation);

        // 변경된 예약 반환
        return ReservationDTO.toReservationDTO(updatedReservation);
    }

//    @Transactional
//    public ReservationDTO createReservation(Integer memberId, ReservationDTO reservationDTO) {
//        Member member = memberRepository.findById(memberId)
//            .orElseThrow(MemberNotFoundException::new);
//
//        Reservation reservation = Reservation.builder()
//            .member(member)
//            .date(reservationDTO.getDate())
//            .amount(reservationDTO.getAmount())
//            .comment(reservationDTO.getComment())
//            .isRefunded(reservationDTO.isRefunded())
//            .build();
//
//        List<ReservationDetail> reservationDetails = reservationDTO.getReservationDetails()
//            .stream()
//            .map(detailDTO -> {
//                ShowSchedule showSchedule = showScheduleRepository.findById(
//                        detailDTO.getShowScheduleId())
//                    .orElseThrow(ShowScheduleNotFoundException::new);
//
//                return ReservationDetail.builder()
//                    .row(detailDTO.getRow())
//                    .col(detailDTO.getCol())
//                    .showSchedule(showSchedule)
//                    .reservation(reservation)
//                    .build();
//            })
//            .toList();
//
//        reservationDetails.forEach(reservation::addReservationDetail);
//
//        // Member와 Reservation의 관계 설정
//        member.addReservation(reservation);
//        Reservation createdReservation = reservationRepository.save(reservation);
//
//        // Reservation 저장
//        return ReservationDTO.toReservationDTO(createdReservation);
//    }

    @Transactional
    public ReservationDTO createReservation(Integer memberId, ReservationDTO reservationDTO) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        Reservation reservation = Reservation.builder()
            .member(member)
            .date(reservationDTO.getDate())
            .amount(reservationDTO.getAmount())
            .comment(reservationDTO.getComment())
            .isRefunded(reservationDTO.isRefunded())
            .build();

        // ReservationDetails 생성 및 추가
        List<ReservationDetail> reservationDetails = reservationDTO.getReservationDetails()
            .stream()
            .map(detailDTO -> {
                ShowSchedule showSchedule = showScheduleRepository.findById(
                        detailDTO.getShowScheduleId())
                    .orElseThrow(ShowScheduleNotFoundException::new);

                // ReservationDetail 생성
                ReservationDetail reservationDetail = ReservationDetail.builder()
                    .row(detailDTO.getRow())
                    .col(detailDTO.getCol())
                    .showSchedule(showSchedule)
                    .reservation(reservation) // Reservation 설정
                    .build();

                // Reservation에 ReservationDetail 추가
                reservation.addReservationDetail(reservationDetail);

                return reservationDetail;
            })
            .toList();

        // Reservation과 ReservationDetail을 각각 저장
        Reservation createdReservation = reservationRepository.save(reservation);
        reservationDetailRepository.saveAll(reservationDetails); // ReservationDetail 별도 저장

        // 예매상세에 변경사항 반영
        reservation.getReservationDetails()
            .forEach(reservationDetail -> disableReservationService.disableReservation(
                reservationDetail.getShowSchedule().getId(),
                Integer.valueOf(reservationDetail.getRow()),
                Integer.valueOf(reservationDetail.getCol())
            ));

        // DTO로 변환하여 반환
        return ReservationDTO.toReservationDTO(createdReservation);
    }

    // 입력받은 show_schedule_id로 Reservation을 조회하고 카운트 증가
    @Transactional(readOnly = true)
    public long countReservedSeatsByShowScheduleId(Long showScheduleId) {
        // 주어진 show_schedule_id로 Reservation을 조회
        List<Reservation> reservations = reservationRepository.findByReservationDetailsShowScheduleId(
            showScheduleId);

        // 각 Reservation의 ReservationDetail 수를 카운트
        long reservedSeatsCount = reservations.stream()
            .flatMap(reservation -> reservation.getReservationDetails().stream())
            .filter(detail -> detail.getShowSchedule().getId().equals(showScheduleId))
            .count();

        return reservedSeatsCount;
    }


}
