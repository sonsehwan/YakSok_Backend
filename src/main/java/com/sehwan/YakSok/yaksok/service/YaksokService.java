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
    public void saveYaksok(YaksokRequest request){
        Yaksok yaksok = Yaksok.builder()
                .name(request.getName())
                .date(request.getDate())
                .build();

        for(PillRequest pillRequest : request.getPills()){
            Pill pill = Pill.builder()
                    .name(pillRequest.getName())
                    .image(pillRequest.getImage())
                    .dayFrequency(pillRequest.getDayFrequency())
                    .duration(pillRequest.getDuration())
                    .dayDosage(pillRequest.getDayDosage())
                    .build();
            yaksok.addPill(pill);
        }

        yaksokRepository.save(yaksok);
    }
}
