package com.sehwan.YakSok.chat.service;

import com.sehwan.YakSok.chat.dto.ChatMessageDto;
import com.sehwan.YakSok.chat.dto.request.ChatRoomRequest;
import com.sehwan.YakSok.chat.dto.request.FriendChatRoomRequest;
import com.sehwan.YakSok.chat.dto.response.ChatRoomListDto;
import com.sehwan.YakSok.chat.dto.response.ChatRoomResponse;
import com.sehwan.YakSok.chat.entity.ChatMessage;
import com.sehwan.YakSok.chat.entity.ChatParticipant;
import com.sehwan.YakSok.chat.entity.ChattingRoom;
import com.sehwan.YakSok.chat.entity.RoomType;
import com.sehwan.YakSok.chat.repository.ChatParticipantRepository;
import com.sehwan.YakSok.chat.repository.ChatRepository;
import com.sehwan.YakSok.chat.repository.ChattingRoomRepository;
import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.drugstore.repository.DrugStoreRepository;
import com.sehwan.YakSok.user.entity.FriendRelationId;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.FriendRelationRepository;
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
    private final DrugStoreRepository drugStoreRepository;
    private final FriendRelationRepository friendRelationRepository;

    // ------------------------------------채팅 메시지 관련 로직------------------------------------------------------------

    // 받은 채팅 저장. 저장 결과를 그대로 브로드캐스트하도록 DTO를 돌려준다.
    @Transactional
    public ChatMessageDto saveMessage(ChatMessageDto message){
        Long roomId = Long.valueOf(message.getRoomId());

        // 발신자가 이 방의 실제 참여자인지 확인한다.
        // (클라가 보낸 id라 진짜 사칭 방지는 STOMP 메시지 인증 도입 때 완성된다.)
        ChatParticipant sender = chatParticipantRepository
                .findWithRefsByIdAndRoomId(message.getSenderParticipantId(), roomId)
                .orElseThrow(() -> new IllegalArgumentException("해당 채팅방의 참여자가 아닙니다."));

        ChatMessage entity = message.toEntity();
        entity.setSenderName(sender.displayName());   // 보낼 당시 이름 스냅샷
        ChatMessage saved = chatRepository.save(entity);

        // 메시지가 오면 방을 삭제(숨김)했던 참여자에게도 방을 다시 노출시킨다.
        chatParticipantRepository.findByChattingRoomIdAndHiddenTrue(roomId)
                .forEach(p -> p.setHidden(false));

        return ChatMessageDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatMessages(Long userId, Long roomId){
        ChatParticipant participant = resolveMyParticipant(roomId, userId);

        List<ChatMessage> list = participant.getJoinedAt() != null
                ? chatRepository.findByRoomIdAndCreatedAtAfterOrderByCreatedAtAsc(String.valueOf(roomId), participant.getJoinedAt())
                : chatRepository.findByRoomIdOrderByCreatedAtAsc(String.valueOf(roomId));

        // 이름은 메시지에 스냅샷돼 있으므로 참여자를 다시 조회할 필요가 없다.
        return list.stream()
                .map(ChatMessageDto::from)
                .collect(Collectors.toList());
    }


    // ------------------------------------채팅방 관련 로직------------------------------------------------------------

    // 약국 상담방: 있으면 가져오고 없으면 [고객 User, 약국 DrugStore]로 생성한다.
    @Transactional
    public ChatRoomResponse getOrCreateRoom(ChatRoomRequest request, Long userId){

        User customer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("현재 로그인한 회원 정보를 찾지 못했습니다."));

        DrugStore drugStore = drugStoreRepository.findByHpid(request.getHpid())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 약국입니다."));

        if (!userRepository.existsByMyDrugStore_Hpid(request.getHpid())) {
            throw new RuntimeException("아직 채팅 상담을 지원하지 않는 약국입니다.");
        }

        // 자기 약국에는 채팅을 시작할 수 없다. (약국이 유저 참여자가 아니므로 hpid로 비교한다.)
        if (customer.getMyDrugStore() != null
                && request.getHpid().equals(customer.getMyDrugStore().getHpid())) {
            throw new RuntimeException("자신의 약국에는 채팅을 시작할 수 없습니다.");
        }

        Optional<ChattingRoom> existingRoom = chattingRoomRepository.findConsultRoom(userId, request.getHpid());

        if(existingRoom.isPresent()){
            log.info("이미 존재하는 상담방입니다. 방 번호 {}", existingRoom.get().getId());
            return new ChatRoomResponse(existingRoom.get().getId(), myParticipantId(existingRoom.get(), userId), false);
        }

        ChattingRoom newRoom = new ChattingRoom();
        newRoom.setRoomType(RoomType.PHARMACY_CONSULT);
        newRoom.setRoomName(drugStore.getDutyName() + " 상담방");

        newRoom.addParticipant(new ChatParticipant(customer, newRoom));
        newRoom.addParticipant(new ChatParticipant(drugStore, newRoom));

        chattingRoomRepository.save(newRoom);

        log.info("새로운 상담방 생성 완료: 방 번호 {}", newRoom.getId());
        return new ChatRoomResponse(newRoom.getId(), myParticipantId(newRoom, userId), true);
    }

    // 친구와의 1:1 채팅방: 있으면 가져오고 없으면 [User, User]로 생성한다. 실제 친구일 때만.
    @Transactional
    public ChatRoomResponse getOrCreateFriendRoom(FriendChatRoomRequest request, Long userId){

        User me = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("현재 로그인한 회원 정보를 찾지 못했습니다."));

        User friend = userRepository.findById(request.getFriendId())
                .orElseThrow(() -> new RuntimeException("상대방 회원 정보를 찾지 못했습니다."));

        if (me.getId().equals(friend.getId())) {
            throw new RuntimeException("자기 자신과는 채팅할 수 없습니다.");
        }

        // 실제 친구 관계일 때만 방을 만든다.
        if (!friendRelationRepository.existsById(new FriendRelationId(me.getId(), friend.getId()))) {
            throw new RuntimeException("친구인 사용자와만 채팅방을 만들 수 있습니다.");
        }

        Optional<ChattingRoom> existingRoom = chattingRoomRepository.findDirectRoom(me.getId(), friend.getId());

        if(existingRoom.isPresent()){
            log.info("이미 존재하는 친구 채팅방입니다. 방 번호 {}", existingRoom.get().getId());
            return new ChatRoomResponse(existingRoom.get().getId(), myParticipantId(existingRoom.get(), userId), false);
        }

        ChattingRoom newRoom = new ChattingRoom();
        newRoom.setRoomType(RoomType.DIRECT);
        newRoom.setRoomName(friend.getNickname());

        newRoom.addParticipant(new ChatParticipant(me, newRoom));
        newRoom.addParticipant(new ChatParticipant(friend, newRoom));

        chattingRoomRepository.save(newRoom);

        log.info("새로운 친구 채팅방 생성 완료: 방 번호 {}", newRoom.getId());
        return new ChatRoomResponse(newRoom.getId(), myParticipantId(newRoom, userId), true);
    }

    // 로그인 유저가 유저 자격으로 참여 중인 채팅방 목록 (친구방 + 내가 고객인 상담방)
    @Transactional(readOnly = true)
    public List<ChatRoomListDto> getMyChatRooms(Long userId){
        User loginUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 회원정보를 찾지 못했습니다."));

        List<ChattingRoom> rooms = chattingRoomRepository.findAllAsUserByUserId(loginUser.getId());
        return toRoomList(rooms, loginUser.getId(), null);
    }

    // 약국 사장의 상담 채팅방 목록. 약국 미등록이면 빈 목록.
    @Transactional(readOnly = true)
    public List<ChatRoomListDto> getConsultRooms(Long userId){
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 회원정보를 찾지 못했습니다."));

        if (owner.getMyDrugStore() == null) {
            return List.of();
        }

        String hpid = owner.getMyDrugStore().getHpid();
        return toRoomList(chattingRoomRepository.findAllConsultByDrugStoreHpid(hpid), null, hpid);
    }

    @Transactional
    public List<ChatRoomListDto> deleteChattingRoom(Long userId, Long roomId) {
        ChatParticipant chatParticipant = chatParticipantRepository.findByChattingRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 연결된 채팅방 관계가 없습니다."));

        chatParticipant.setHidden(true);
        chatParticipant.setJoinedAt(LocalDateTime.now());

        return getMyChatRooms(userId);
    }

    // 회원 탈퇴 시 채팅방 정리. 유저 삭제 직전에 UserService에서 호출한다.
    // 참여자를 빼고 나서 그 방에 남은 참여자가 0명이면 방과 메시지를 함께 지운다.
    @Transactional
    public void handleUserWithdrawal(Long userId) {
        List<ChatParticipant> myParticipations = chatParticipantRepository.findByUserId(userId);

        for (ChatParticipant mine : myParticipations) {
            Long roomId = mine.getChattingRoom().getId();
            chatParticipantRepository.delete(mine);
            chatParticipantRepository.flush();

            if (chatParticipantRepository.countByChattingRoomId(roomId) == 0) {
                chatRepository.deleteAllByRoomId(String.valueOf(roomId));
                chattingRoomRepository.deleteById(roomId);
                log.info("참여자 0명 → 방/메시지 삭제. 방 번호 {}", roomId);
            }
        }
    }

    // ------------------------------------헬퍼------------------------------------------------------------

    // 이 방에서 나(userId)를 대표하는 USER 참여자의 id. 방 생성 직후 호출한다.
    private Long myParticipantId(ChattingRoom room, Long userId) {
        ChatParticipant mine = myParticipantOf(room, userId, null);
        if (mine == null) {
            throw new IllegalStateException("방에서 내 참여자 정보를 찾지 못했습니다.");
        }
        return mine.getId();
    }

    // roomId만 있을 때 호출자의 참여자를 찾는다. USER(userId)로 먼저 찾고,
    // 없으면 약국 사장인 경우 그 약국(DRUGSTORE)으로 찾는다.
    private ChatParticipant resolveMyParticipant(Long roomId, Long userId) {
        Optional<ChatParticipant> asUser = chatParticipantRepository.findByChattingRoomIdAndUserId(roomId, userId);
        if (asUser.isPresent()) {
            return asUser.get();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 회원정보를 찾지 못했습니다."));
        if (user.getMyDrugStore() != null) {
            Optional<ChatParticipant> asStore = chatParticipantRepository
                    .findByChattingRoomIdAndDrugStore_Hpid(roomId, user.getMyDrugStore().getHpid());
            if (asStore.isPresent()) {
                return asStore.get();
            }
        }
        throw new IllegalArgumentException("해당 채팅방에 참여하고 있지 않습니다.");
    }

    // 방 참여자 목록에서 '나'를 찾는다. myUserId(개인 목록) 또는 myHpid(사장 상담함) 중 하나로 판별.
    private ChatParticipant myParticipantOf(ChattingRoom room, Long myUserId, String myHpid) {
        return room.getParticipants().stream()
                .filter(p -> myUserId != null
                        ? (p.isUser() && p.getUser().getId().equals(myUserId))
                        : (p.isDrugStore() && p.getDrugStore().getHpid().equals(myHpid)))
                .findFirst()
                .orElse(null);
    }

    private List<ChatRoomListDto> toRoomList(List<ChattingRoom> rooms, Long myUserId, String myHpid) {
        if (rooms.isEmpty()) {
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
                    ChatParticipant mine = myParticipantOf(room, myUserId, myHpid);

                    return new ChatRoomListDto(
                            room.getId(),
                            mine != null ? mine.getId() : null,
                            resolveRoomName(room, myUserId, myHpid),
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

    // 나를 제외한 상대 참여자의 이름. myUserId(개인 목록) 또는 myHpid(사장 상담함) 중 하나로 '나'를 가린다.
    private String resolveRoomName(ChattingRoom room, Long myUserId, String myHpid) {
        return room.getParticipants().stream()
                .filter(p -> myUserId != null
                        ? !(p.isUser() && p.getUser().getId().equals(myUserId))
                        : !(p.isDrugStore() && p.getDrugStore().getHpid().equals(myHpid)))
                .findFirst()
                .map(ChatParticipant::displayName)
                .orElse(room.getRoomName());
    }
}
