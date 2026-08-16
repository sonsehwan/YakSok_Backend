package com.sehwan.YakSok.yaksok.service;

import com.sehwan.YakSok.chat.repository.ChatRepository;
import com.sehwan.YakSok.chat.repository.ChattingRoomRepository;
import com.sehwan.YakSok.user.dto.response.SharedUser;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.UserRepository;
import com.sehwan.YakSok.yaksok.dto.YaksokDto;
import com.sehwan.YakSok.yaksok.entity.ShareYaksok;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import com.sehwan.YakSok.yaksok.repository.ShareYaksokRepository;
import com.sehwan.YakSok.yaksok.repository.YaksokRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareYaksokService {

    private final UserRepository userRepository;
    private final ChattingRoomRepository chattingRoomRepository;
    private final ChatRepository chatRepository;
    private final YaksokRepository yaksokRepository;
    private final ShareYaksokRepository shareYaksokRepository;

    // 공유받은 약속을 내 목록에 저장한다.
    @Transactional
    public void saveSharedYaksok(Long userId, Long yaksokId){
        User receiver = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Yaksok yaksok = yaksokRepository.findById(yaksokId)
                .orElseThrow(()-> new IllegalArgumentException("해당 약속이 존재하지 않습니다. id = " + yaksokId));

        if(yaksok.getUser().getId().equals(receiver.getId())){
            throw new IllegalStateException("자기 약속은 공유 목록에 저장할 수 없습니다.");
        }

        if(shareYaksokRepository.existsByReceiverIdAndYaksokId(receiver.getId(), yaksokId)){
            throw new IllegalStateException("이미 저장한 공유 약속입니다.");
        }

        validateSharedToMe(userId, yaksokId);

        shareYaksokRepository.save(ShareYaksok.create(receiver, yaksok));
    }

    private void validateSharedToMe(Long userId, Long yaksokId){
        List<Long> myRoomIds = chattingRoomRepository.findRoomIdByUserId(userId);

        if(myRoomIds.isEmpty()){
            throw new IllegalStateException("공유받지 않은 약속입니다.");
        }

        List<String> roomIds = myRoomIds.stream()
                .map(String::valueOf)
                .toList();

        if(chatRepository.countSharedInRooms(yaksokId, roomIds)== 0){
            throw new IllegalStateException("공유받지 않은 약속입니다.");
        }
    }

    // 내게 약속을 공유한 user의 목록
    @Transactional
    public List<SharedUser> getSharedUserList(Long userId){
        return shareYaksokRepository.findDistinctBySendersByReceiverId(userId).stream()
                .map(SharedUser::from)
                .toList();
    }

    // 저장한 공유 약속 목록
    @Transactional
    public List<YaksokDto> getSharedYaksokList(Long userId, Long senderId) {
        List<ShareYaksok> shares = (senderId != null)
                ? shareYaksokRepository.findAllByReceiverIdAndSenderId(userId, senderId)
                : shareYaksokRepository.findAllByReceiverId(userId);

        return shares.stream()
                .map(share -> YaksokDto.from(share.getYaksok()))
                .toList();
    }

    // 내 목록에서 빼기. 저장한 본인만 가능하다.
    @Transactional
    public void deleteSharedYaksok(Long userId, Long yaksokId) {
        ShareYaksok share = shareYaksokRepository
                .findByReceiverIdAndYaksokId(userId, yaksokId)
                .orElseThrow(() -> new IllegalArgumentException("저장한 공유 약속이 아닙니다."));

        shareYaksokRepository.delete(share);
    }
}
