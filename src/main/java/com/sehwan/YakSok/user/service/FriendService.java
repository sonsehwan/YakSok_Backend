package com.sehwan.YakSok.user.service;

import com.sehwan.YakSok.user.entity.FriendRelation;
import com.sehwan.YakSok.user.entity.FriendRequest;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.FriendRelationRepository;
import com.sehwan.YakSok.user.repository.FriendRequestRepository;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendRelationRepository friendRelationRepository;

    /**
     *  친구 요청
     */
    // 친구 요청 생성
    @Transactional
    public void createFriendRequest(Long userId, Long friendId){

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저입니다."));

        User friend = userRepository.findById(friendId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 유저입니다."));

        FriendRequest friendRequest = FriendRequest.create(user, friend);

        friendRequestRepository.save(friendRequest);
    }

    // 친구 수락
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

    /**
     *  친구 관계
     */
}
