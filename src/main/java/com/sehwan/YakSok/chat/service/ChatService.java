package com.sehwan.YakSok.chat.service;

import com.sehwan.YakSok.chat.dto.ChatMessageDto;
import com.sehwan.YakSok.chat.dto.request.ChatRoomRequest;
import com.sehwan.YakSok.chat.dto.request.FriendChatRoomRequest;
import com.sehwan.YakSok.chat.dto.response.ChatRoomListDto;
import com.sehwan.YakSok.chat.dto.response.ChatRoomResponse;
import com.sehwan.YakSok.chat.entity.ChatMessage;
import com.sehwan.YakSok.chat.entity.ChatParticipant;
import com.sehwan.YakSok.chat.entity.ChattingRoom;
import com.sehwan.YakSok.chat.repository.ChatParticipantRepository;
import com.sehwan.YakSok.chat.repository.ChatRepository;
import com.sehwan.YakSok.chat.repository.ChattingRoomRepository;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChattingRoomRepository chattingRoomRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final ChatParticipantRepository chatParticipantRepository;

    // ------------------------------------채팅 메시지 관련 로직------------------------------------------------------------

    // 받은 채팅 저장. 저장 결과를 그대로 브로드캐스트하도록 DTO를 돌려준다.
    @Transactional
    public ChatMessageDto saveMessage(ChatMessageDto message){
        ChatMessage saved = chatRepository.save(message.toEntity());

        // 메시지가 오면 방을 삭제(숨김)했던 참여자에게도 방을 다시 노출시킨다.
        List<ChatParticipant> hiddenParticipants = chatParticipantRepository
                .findByChattingRoomIdAndHiddenTrue(Long.valueOf(saved.getRoomId()));
        hiddenParticipants.forEach(p -> p.setHidden(false));

        String nickname = userRepository.findByEmail(saved.getSender())
                .map(User::getNickname)
                .orElse(saved.getSender());

        return ChatMessageDto.from(saved, nickname);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatMessages(Long userId, Long roomId){
        ChatParticipant participant = chatParticipantRepository.findByChattingRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방에 참여하고 있지 않습니다."));

        List<ChatMessage> list = participant.getJoinedAt() != null
                ? chatRepository.findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(String.valueOf(roomId), participant.getJoinedAt())
                : chatRepository.findByRoomIdOrderByCreatedAtAsc(String.valueOf(roomId));

        Set<String> senders = list.stream()
                .map(ChatMessage::getSender)
                .collect(Collectors.toSet());

        Map<String, String> nicknames = userRepository.findByEmailIn(senders).stream()
                .collect(Collectors.toMap(User::getEmail, User::getNickname));

        return list.stream()
                .map(m -> ChatMessageDto.from(m, nicknames.getOrDefault(m.getSender(), m.getSender())))
                .collect(Collectors.toList());
    }


    // ------------------------------------채팅방 관련 로직------------------------------------------------------------

    // 채팅방이 있으면 가져오고 없으면 생성하여 유저들 참가시키기
    @Transactional
    public ChatRoomResponse getOrCreateRoom(ChatRoomRequest request, Long userId){

        User clientUser = userRepository.findById(userId)
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

    // 친구와의 1:1 채팅방이 있으면 가져오고 없으면 생성한다.
    @Transactional
    public ChatRoomResponse getOrCreateFriendRoom(FriendChatRoomRequest request, Long userId){

        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("현재 로그인한 회원 정보를 찾지 못했습니다."));

        User friend = userRepository.findById(request.getFriendId())
                .orElseThrow(() -> new RuntimeException("상대방 회원 정보를 찾지 못했습니다."));

        if (me.getId().equals(friend.getId())) {
            throw new RuntimeException("자기 자신과는 채팅할 수 없습니다.");
        }

        Optional<ChattingRoom> existingRoom = chattingRoomRepository.findExistingRoom(me, friend);

        if(existingRoom.isPresent()){
            log.info("이미 존재하는 친구 채팅방입니다. 방 번호 {}", existingRoom.get().getId());
            return new ChatRoomResponse(existingRoom.get().getId(), false);
        }

        ChattingRoom newRoom = new ChattingRoom();
        newRoom.setRoomName(friend.getNickname());

        newRoom.addParticipant(new ChatParticipant(me, newRoom));
        newRoom.addParticipant(new ChatParticipant(friend, newRoom));

        chattingRoomRepository.save(newRoom);

        log.info("새로운 친구 채팅방 생성 완료: 방 번호 {}", newRoom.getId());
        return new ChatRoomResponse(newRoom.getId(), true);
    }

    // 로그인 유저가 참여중인 채팅방 리스트 목록
    @Transactional(readOnly = true)
    public List<ChatRoomListDto> getMyChatRooms(Long userId){
        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 회원정보를 찾지 못했습니다."));

        List<ChattingRoom> rooms = chattingRoomRepository.findAllWithParticipantsByUserId(loginUser.getId());

        if(rooms.isEmpty()){
            return List.of();
        }

        List<String> roomIds = rooms.stream()
                .map(room -> String.valueOf(room.getId()))
                .toList();

        Map<String, ChatMessage> lastMessage = chatRepository.findLastMessagesByRoomIds(roomIds).stream()
                .collect(Collectors.toMap(ChatMessage::getRoomId, m -> m));

        return rooms.stream()
                .map(room -> {
                    ChatMessage last = lastMessage.get(String.valueOf(room.getId()));

                    return new ChatRoomListDto(
                            room.getId(),
                            resolveRoomName(room, loginUser.getId()),
                            last != null ? last.getMessage() : null,
                            last != null ? last.getCreatedAt() : null
                    );
                })
                .sorted(Comparator.comparing(
                        ChatRoomListDto::getLastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());
    }

    // 방이름을 직접 조회유저에 맞게 생성
    private String resolveRoomName(ChattingRoom room, Long myUserId) {
        return room.getParticipants().stream()
                .map(ChatParticipant::getUser)
                .filter(Objects::nonNull)
                .filter(participant -> !participant.getId().equals(myUserId))
                .findFirst()
                .map(other -> other.getMyDrugStore() != null
                        ? other.getMyDrugStore().getDutyName()
                        : other.getNickname())
                .orElse(room.getRoomName());
    }

    @Transactional
    public List<ChatRoomListDto> deleteChattingRoom(Long userId, Long roomId) {
        ChatParticipant chatParticipant = chatParticipantRepository.findByChattingRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 연결된 채팅방 관계가 없습니다."));

        chatParticipant.setHidden(true);
        chatParticipant.setJoinedAt(LocalDateTime.now());

        return getMyChatRooms(userId);
    }
}
