package com.sehwan.YakSok.user.service;

import com.sehwan.YakSok.user.dto.response.UserProfileDto;
import com.sehwan.YakSok.user.entity.FriendQrCode;
import com.sehwan.YakSok.user.entity.FriendRelationId;
import com.sehwan.YakSok.user.entity.FriendRequest;
import com.sehwan.YakSok.user.entity.RelationStatus;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.FriendQrCodeRepository;
import com.sehwan.YakSok.user.repository.FriendRelationRepository;
import com.sehwan.YakSok.user.repository.FriendRequestRepository;
import com.sehwan.YakSok.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendServiceQrProfileTest {

    @Mock private UserRepository userRepository;
    @Mock private FriendRequestRepository friendRequestRepository;
    @Mock private FriendRelationRepository friendRelationRepository;
    @Mock private FriendQrCodeRepository friendQrCodeRepository;

    private static final Long ME = 1L;
    private static final Long OTHER = 2L;
    private static final String CODE = "test-code";

    private FriendService newService() {
        return new FriendService(userRepository, friendRequestRepository, friendRelationRepository, friendQrCodeRepository);
    }

    private User user(Long id) {
        return User.builder().id(id).nickname("user" + id).email("user" + id + "@test.com").build();
    }

    @Test
    void 만료된_코드는_예외() {
        FriendQrCode qrCode = FriendQrCode.create(user(OTHER));
        ReflectionTestUtils.setField(qrCode, "expiresAt", LocalDateTime.now().minusMinutes(1));
        when(friendQrCodeRepository.findByCode(CODE)).thenReturn(Optional.of(qrCode));

        assertThrows(IllegalStateException.class,
                () -> newService().getUserProfileByQrCode(CODE, ME));
    }

    @Test
    void 존재하지_않는_코드는_예외() {
        when(friendQrCodeRepository.findByCode(CODE)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> newService().getUserProfileByQrCode(CODE, ME));
    }

    @Test
    void 본인_코드면_SELF() {
        when(friendQrCodeRepository.findByCode(CODE)).thenReturn(Optional.of(FriendQrCode.create(user(ME))));

        UserProfileDto result = newService().getUserProfileByQrCode(CODE, ME);

        assertEquals(RelationStatus.SELF, result.getRelation());
    }

    @Test
    void 이미_친구면_FRIEND() {
        when(friendQrCodeRepository.findByCode(CODE)).thenReturn(Optional.of(FriendQrCode.create(user(OTHER))));
        when(friendRelationRepository.existsById(new FriendRelationId(ME, OTHER))).thenReturn(true);

        UserProfileDto result = newService().getUserProfileByQrCode(CODE, ME);

        assertEquals(RelationStatus.FRIEND, result.getRelation());
    }

    @Test
    void 대기중인_요청이_없으면_NONE() {
        when(friendQrCodeRepository.findByCode(CODE)).thenReturn(Optional.of(FriendQrCode.create(user(OTHER))));
        when(friendRelationRepository.existsById(new FriendRelationId(ME, OTHER))).thenReturn(false);
        when(friendRequestRepository.findByUserLowIdAndUserHighId(ME, OTHER)).thenReturn(Optional.empty());

        UserProfileDto result = newService().getUserProfileByQrCode(CODE, ME);

        assertEquals(RelationStatus.NONE, result.getRelation());
    }

    @Test
    void 내가_보낸_대기중_요청이면_SENT() {
        when(friendQrCodeRepository.findByCode(CODE)).thenReturn(Optional.of(FriendQrCode.create(user(OTHER))));
        when(friendRelationRepository.existsById(new FriendRelationId(ME, OTHER))).thenReturn(false);

        FriendRequest pending = FriendRequest.create(user(ME), user(OTHER)); // 내가 보낸 요청
        when(friendRequestRepository.findByUserLowIdAndUserHighId(ME, OTHER)).thenReturn(Optional.of(pending));

        UserProfileDto result = newService().getUserProfileByQrCode(CODE, ME);

        assertEquals(RelationStatus.SENT, result.getRelation());
    }

    @Test
    void 상대가_보낸_대기중_요청이면_RECEIVED_요청id포함() {
        when(friendQrCodeRepository.findByCode(CODE)).thenReturn(Optional.of(FriendQrCode.create(user(OTHER))));
        when(friendRelationRepository.existsById(new FriendRelationId(ME, OTHER))).thenReturn(false);

        FriendRequest pending = FriendRequest.create(user(OTHER), user(ME)); // 상대가 보낸 요청
        when(friendRequestRepository.findByUserLowIdAndUserHighId(ME, OTHER)).thenReturn(Optional.of(pending));

        UserProfileDto result = newService().getUserProfileByQrCode(CODE, ME);

        assertEquals(RelationStatus.RECEIVED, result.getRelation());
        assertEquals(pending.getId(), result.getRequestId());
    }

    @Test
    void 거절된_요청은_NONE_취급() {
        when(friendQrCodeRepository.findByCode(CODE)).thenReturn(Optional.of(FriendQrCode.create(user(OTHER))));
        when(friendRelationRepository.existsById(new FriendRelationId(ME, OTHER))).thenReturn(false);

        FriendRequest rejected = FriendRequest.create(user(ME), user(OTHER));
        rejected.reject();
        when(friendRequestRepository.findByUserLowIdAndUserHighId(ME, OTHER)).thenReturn(Optional.of(rejected));

        UserProfileDto result = newService().getUserProfileByQrCode(CODE, ME);

        assertEquals(RelationStatus.NONE, result.getRelation());
    }
}
