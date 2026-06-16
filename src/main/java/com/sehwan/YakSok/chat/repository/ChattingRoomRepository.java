package com.sehwan.YakSok.chat.repository;

import com.sehwan.YakSok.chat.entity.ChattingRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChattingRoomRepository extends JpaRepository<ChattingRoom, Long> {
}
