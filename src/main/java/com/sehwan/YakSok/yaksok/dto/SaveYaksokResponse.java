package com.sehwan.YakSok.yaksok.dto;

import com.sehwan.YakSok.yaksok.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@AllArgsConstructor
public class SaveYaksokResponse {
    Long Id;
    List<Notification> notificationList = new ArrayList<>();
}
