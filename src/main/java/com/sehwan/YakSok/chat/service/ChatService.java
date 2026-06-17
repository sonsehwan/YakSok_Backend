package com.sehwan.YakSok.chat.service;

import com.sehwan.YakSok.chat.dto.request.ChatRoomRequest;
import com.sehwan.YakSok.chat.dto.response.ChatRoomResponse;
import com.sehwan.YakSok.chat.entity.ChatParticipant;
import com.sehwan.YakSok.chat.entity.ChattingRoom;
import com.sehwan.YakSok.chat.repository.ChattingRoomRepository;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChattingRoomRepository chattingRoomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoomResponse getOrCreateRoom(ChatRoomRequest request){

        User clientUser = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("현재 로그인한 회원 정보를 찾지 못했습니다."));

        User pharmacyUser = userRepository.findByMyDrugStore_Hpid(request.getHpid())
                .orElseThrow(()-> new RuntimeException("아직 채팅 상담을 지원하지 않는 약국입니다."));

        Optional<ChattingRoom> existingRoom = chattingRoomRepository.findExistingRoom(clientUser, pharmacyUser);

        if(existingRoom.isPresent()){
            log.info("이미 존재하는 채팅방입니다. 방 번호 {}", existingRoom.get().getId());
            return new ChatRoomResponse(existingRoom.get().getId(), false);
        }

        ChattingRoom newRoom = new ChattingRoom();
        newRoom.setRoomName(pharmacyUser.getMyDrugStore().getDutyName() + " 상담방");

        newRoom.addParticipant(new ChatParticipant(clientUser, newRoom));
        newRoom.addParticipant(new ChatParticipant(pharmacyUser, newRoom));

        chattingRoomRepository.save(newRoom);

        log.info("새로운 채팅방 생성 완료: 방 번호 {}", newRoom.getId());
        return new ChatRoomResponse(newRoom.getId(), true);
    }
}
