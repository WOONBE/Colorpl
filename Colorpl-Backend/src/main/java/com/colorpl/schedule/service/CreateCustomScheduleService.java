package com.colorpl.schedule.service;

import com.colorpl.global.common.exception.MemberNotFoundException;
import com.colorpl.global.storage.StorageService;
import com.colorpl.global.storage.UploadFile;
import com.colorpl.member.Member;
import com.colorpl.member.repository.MemberRepository;
import com.colorpl.member.service.MemberService;
import com.colorpl.schedule.domain.CustomSchedule;
import com.colorpl.schedule.repository.ScheduleRepository;
import com.colorpl.schedule.dto.CreateCustomScheduleRequest;
import com.colorpl.show.domain.Category;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
@Transactional
public class CreateCustomScheduleService {

    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final ScheduleRepository scheduleRepository;
    private final StorageService storageService;

    public Long create(
        CreateCustomScheduleRequest request,
        MultipartFile attachFile
    ) {

        Integer memberId = memberService.getCurrentMemberId();

        Member member = memberRepository.findById(memberId)
            .orElseThrow(MemberNotFoundException::new);

        UploadFile uploadFile = storageService.storeFile(attachFile);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm");

        CustomSchedule customSchedule = CustomSchedule.builder()
            .member(member)
            .image(uploadFile.getStoreFilename())
            .seat(request.getSeat())
            .dateTime(LocalDateTime.parse(request.getDateTime(), formatter))
            .name(request.getName())
            .category(Category.fromString(request.getCategory()).orElseThrow())
            .location(request.getLocation())
            .latitude(request.getLatitude())
            .longitude(request.getLongitude())
            .build();
        scheduleRepository.save(customSchedule);

        return customSchedule.getId();
    }
}
