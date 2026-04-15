package com.sehwan.YakSok.yaksok.service;

import com.sehwan.YakSok.yaksok.dto.PillRequest;
import com.sehwan.YakSok.yaksok.dto.YaksokRequest;
import com.sehwan.YakSok.yaksok.entity.Pill;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import com.sehwan.YakSok.yaksok.repository.PillRepository;
import com.sehwan.YakSok.yaksok.repository.YaksokRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class YaksokService {

    private final PillRepository pillRepository;
    private final YaksokRepository yaksokRepository;

    @Transactional
    public Long saveYaksok(YaksokRequest request){
        Yaksok yaksok = Yaksok.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .prescriptionDays(request.getPrescriptionDays())
                .takeMorning(request.isTakeMorning())
                .takeLunch(request.isTakeLunch())
                .takeDinner(request.isTakeDinner())
                .timeMorning(request.getTimeMorning())
                .timeLunch(request.getTimeLunch())
                .timeDinner(request.getTimeDinner())
                .dosageTime(request.getDosageTime())
                .build();

        if(request.getPills() != null){
            for(PillRequest pillRequest : request.getPills()){
                Pill pill = Pill.builder()
                        .name(pillRequest.getName())
                        .image(pillRequest.getImage())
                        .dailyFrequency(pillRequest.getDailyFrequency())
                        .dosage(pillRequest.getDosage())
                        .build();
                yaksok.addPill(pill);
            }
        }

        Yaksok savedYaksok = yaksokRepository.save(yaksok);
        return  savedYaksok.getId();
    }
}
