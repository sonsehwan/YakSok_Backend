package com.sehwan.YakSok.chat.service;

import com.sehwan.YakSok.chat.dto.ChatMessageDto;
import com.sehwan.YakSok.chat.dto.request.ChatRoomRequest;
import com.sehwan.YakSok.chat.dto.response.ChatRoomResponse;
import com.sehwan.YakSok.chat.entity.ChatMessage;
import com.sehwan.YakSok.chat.entity.ChatParticipant;
import com.sehwan.YakSok.chat.entity.ChattingRoom;
import com.sehwan.YakSok.chat.repository.ChatRepository;
import com.sehwan.YakSok.chat.repository.ChattingRoomRepository;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChattingRoomRepository chattingRoomRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    // ------------------------------------채팅 메시지 관련 로직------------------------------------------------------------

    // 받은 채팅 저장
    @Transactional
    public void saveMessage(ChatMessageDto message){
        ChatMessage chatMessage = message.toEntity();
        chatRepository.save(chatMessage);
    }

    @Transactional
    public List<ChatMessageDto> getChatMessages(String roomId){
        List<ChatMessage> list =  chatRepository.findByRoomIdOrderByCreatedAtAsc(roomId);

        return list.stream()
                .map(ChatMessageDto::from)
                .collect(Collectors.toList());
    }


    // ------------------------------------채팅방 관련 로직------------------------------------------------------------

    // 채팅방이 있으면 가져오고 없으면 생성하여 유저들 참가시키기
    @Transactional
    public ChatRoomResponse getOrCreateRoom(ChatRoomRequest request){

        User clientUser = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("현재 로그인한 회원 정보를 찾지 못했습니다."));

        User pharmacyUser = userRepository.findByMyDrugStore_Hpid(request.getHpid())
                .orElseThrow(()-> new RuntimeException("아직 채팅 상담을 지원하지 않는 약국입니다."));

        // 자기 자신과의 채팅방은 만들지 않는다.
        if (clientUser.getId().equals(pharmacyUser.getId())) {
            throw new RuntimeException("자신의 약국에는 채팅을 시작할 수 없습니다.");
        }

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

    // 로그인 유저가 참여중인 채팅방 리스트 목록
//    @Transactional(readOnly = true)
//    public List<ChatRoomListDto> getMyChatRooms(String email) {
//        List<ChattingRoom> rooms = chattingRoomRepository.findAllByUserEmail(email);
//
//        return rooms.stream()
//                .map(room -> new ChatRoomListDto(room.getId(), room.getRoomName()))
//                .collect(Collectors.toList());
//    }
}
