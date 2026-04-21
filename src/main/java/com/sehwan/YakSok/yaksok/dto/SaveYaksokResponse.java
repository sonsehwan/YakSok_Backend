package com.sehwan.YakSok.yaksok.dto;

import com.sehwan.YakSok.yaksok.entity.Notification;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class SaveYaksokResponse {
    Yaksok yaksok;
    List<Notification> notifications = new ArrayList<>();
}
