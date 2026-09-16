package com.sehwan.YakSok.user.service;

import com.sehwan.YakSok.user.dto.response.FriendListDto;
import com.sehwan.YakSok.user.dto.response.FriendQrCodeDto;
import com.sehwan.YakSok.user.dto.response.ReceivedFriendRequestDto;
import com.sehwan.YakSok.user.dto.response.UserProfileDto;
import com.sehwan.YakSok.user.dto.response.UserSearchResultDto;
import com.sehwan.YakSok.user.entity.*;
import com.sehwan.YakSok.user.repository.FriendQrCodeRepository;
import com.sehwan.YakSok.user.repository.FriendRelationRepository;
import com.sehwan.YakSok.user.repository.FriendRequestRepository;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendRelationRepository friendRelationRepository;
    private final FriendQrCodeRepository friendQrCodeRepository;

    /**
     *  친구 요청
     */
    // 닉네임으로 친구 추가할 사용자 검색
    @Transactional(readOnly = true)
    public UserSearchResultDto searchUserByNickname(String nickname, Long loginUserId){
        User foundUser = userRepository.findByNickname(nickname)
                .orElseThrow(()-> new IllegalArgumentException("해당 닉네임의 사용자가 없습니다."));

        if(foundUser.getId().equals(loginUserId)){
            throw new IllegalArgumentException("자기 자신은 친구로 추가할 수 없습니다.");
        }

        return UserSearchResultDto.from(foundUser);
    }

    // 친구 요청 생성
    @Transactional
    public void createFriendRequest(Long userId, Long friendId){

        if(userId.equals(friendId)){
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저입니다."));

        User friend = userRepository.findById(friendId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저입니다."));

        Long lowId = Math.min(userId, friendId);
        Long highId = Math.max(userId, friendId);

        Optional<FriendRequest> found = friendRequestRepository.findByUserLowIdAndUserHighId(lowId, highId);

        if(found.isEmpty()){
            friendRequestRepository.save(FriendRequest.create(user, friend));
            return;
        }

        FriendRequest request = found.get();

        if(request.isAccepted()){
            if(friendRelationRepository.existsById(new FriendRelationId(userId, friendId))){
                throw new IllegalStateException("이미 친구인 사용자입니다.");
            }

            friendRelationRepository.save(FriendRelation.create(user, friend));
            return;
        }

        if(request.isPending()){
            if(request.isRequestedBy(userId)){
                throw new IllegalStateException("이미 보낸 친구 요청입니다.");
            }
            acceptAndCreateRelation(request);
            return;
        }

        // 거절된 요청은 이전 방향과 무관하게 이번 요청자를 기준으로 다시 시작한다.
        request.restart(user, friend);
    }

    // 친구 요청을 수락하고 양방향 친구 관계를 만든다.
    private void acceptAndCreateRelation(FriendRequest request){
        User requester = request.getRequester();
        User receiver = request.getReceiver();

        request.accept();

        friendRelationRepository.save(FriendRelation.create(requester, receiver));
        friendRelationRepository.save(FriendRelation.create(receiver, requester));
    }

    // 친구 요청 수락
    @Transactional
    public void acceptFriendRequest(Long requestId, Long loginUserId){
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 친구 요청입니다."));

        if(!friendRequest.isReceivedBy(loginUserId)){
            throw new IllegalArgumentException("친구 요청을 수락할 권한이 없는 유저입니다.");
        }

        User requester = friendRequest.getRequester();
        User receiver = friendRequest.getReceiver();

        friendRequest.accept();

        FriendRelation requesterRelation = FriendRelation.create(requester, receiver);

        FriendRelation receiverRelation = FriendRelation.create(receiver, requester);

        friendRelationRepository.save(requesterRelation);
        friendRelationRepository.save(receiverRelation);
    }

    //친구 요청 거절
    @Transactional
    public void rejectFriendRequest(Long requestId, Long loginUserId){
        FriendRequest friendRequest = friendRequestRepository.findById(requestId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 친구 요청입니다."));

        if(!friendRequest.isReceivedBy(loginUserId)){
            throw new IllegalArgumentException("친구 요청을 거절할 권한이 없는 유저입니다.");
        }

        friendRequest.reject();
    }

    // 로그인 유저가 받은 대기 중인 친구 요청 목록
    @Transactional(readOnly = true)
    public List<ReceivedFriendRequestDto> getReceivedFriendRequests(Long loginUserId){
        List<FriendRequest> requests = friendRequestRepository
                .findReceivedRequests(loginUserId, FriendRequestStatus.PENDING);

        return requests.stream()
                .map(ReceivedFriendRequestDto::from)
                .toList();
    }

    // 내 QR 코드 발급/재발급. "내 QR 보여주기" 화면을 열 때마다 호출해 매번 새로 발급한다(10분 후 만료).
    @Transactional
    public FriendQrCodeDto issueQrCode(Long userId){
        FriendQrCode qrCode = friendQrCodeRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
                    return FriendQrCode.create(user);
                });

        qrCode.reissue();
        friendQrCodeRepository.save(qrCode);

        return FriendQrCodeDto.of(qrCode.getCode());
    }

    // QR 코드로 알아낸 사용자의 프로필 + 나와의 관계 상태
    @Transactional(readOnly = true)
    public UserProfileDto getUserProfileByQrCode(String code, Long loginUserId){
        FriendQrCode qrCode = friendQrCodeRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 코드입니다."));

        if(qrCode.isExpired()){
            throw new IllegalStateException("만료된 코드입니다. 상대방에게 QR을 다시 보여달라고 요청하세요.");
        }

        User user = qrCode.getUser();
        Long userId = user.getId();

        if(userId.equals(loginUserId)){
            return UserProfileDto.of(user, RelationStatus.SELF, null);
        }

        if(friendRelationRepository.existsById(new FriendRelationId(loginUserId, userId))){
            return UserProfileDto.of(user, RelationStatus.FRIEND, null);
        }

        Long lowId = Math.min(loginUserId, userId);
        Long highId = Math.max(loginUserId, userId);

        return friendRequestRepository.findByUserLowIdAndUserHighId(lowId, highId)
                .filter(FriendRequest::isPending)
                .map(request -> request.isRequestedBy(loginUserId)
                        ? UserProfileDto.of(user, RelationStatus.SENT, null)
                        : UserProfileDto.of(user, RelationStatus.RECEIVED, request.getId()))
                .orElseGet(() -> UserProfileDto.of(user, RelationStatus.NONE, null));
    }

    /**
     *  친구 관계
     */

    // 친구 목록
    @Transactional
    public FriendListDto getFriendList(Long loginUserId){
        List<FriendRelation> relations = friendRelationRepository.findByUserId(loginUserId);
        return FriendListDto.from(relations);
    }

    @Transactional
    public FriendListDto deleteFriend(Long loginUserId, Long friendId){
        if(loginUserId.equals(friendId)){
            throw new IllegalArgumentException("자기 자신은 친구 삭제 대상이 아닙니다.");
        }

        FriendRelationId myRelationId = new FriendRelationId(loginUserId, friendId);

        if(!friendRelationRepository.existsById(myRelationId)){
            throw new IllegalArgumentException("이미 친구 관계가 아닙니다.");
        }

        friendRelationRepository.deleteById(myRelationId);

        FriendRelationId reverseRelationId = new FriendRelationId(friendId, loginUserId);

        if(!friendRelationRepository.existsById(reverseRelationId)){
            Long lowId = Math.min(loginUserId, friendId);
            Long highId = Math.max(loginUserId, friendId);

            friendRequestRepository.findByUserLowIdAndUserHighId(lowId, highId)
                    .ifPresent(friendRequestRepository::delete);
        }

        return getFriendList(loginUserId);
    }
}
