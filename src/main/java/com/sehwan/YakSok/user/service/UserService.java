package com.sehwan.YakSok.user.service;


import com.sehwan.YakSok.chat.service.ChatService;
import com.sehwan.YakSok.user.dto.request.ModifyInfoRequest;
import com.sehwan.YakSok.user.dto.request.ModifyPasswordRequest;
import com.sehwan.YakSok.user.dto.response.UserResponse;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserDevice;
import com.sehwan.YakSok.user.repository.UserDeviceRepository;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;

    private final PasswordEncoder passwordEncoder;
    private final ChatService chatService;

    @Transactional
    public UserResponse modifyInfo(ModifyInfoRequest dto, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.setNickname(dto.getNickname());

        return new UserResponse(user);
    }

    @Transactional
    public void modifyPassword(ModifyPasswordRequest dto, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(user.getPassword() == null){
            throw new IllegalArgumentException("카카오 계정은 비밀번호를 변경할 수 없습니다.");
        }

        if(!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. id=" + userId));

        // 채팅방 정리: 참여자에서 빼고, 그 방에 남은 참여자가 없으면 방/메시지까지 삭제
        chatService.handleUserWithdrawal(userId);

        userRepository.delete(user);
    }

    @Transactional
    public void updateToken(Long userId, String token) {

        if(token == null || token.isBlank()){
            throw new IllegalArgumentException("등록할 FCM 토큰이 비어 있습니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        userDeviceRepository.findByToken(token).ifPresentOrElse(
                device -> {
                    device.setUser(user);
                    device.setUpdatedAt(LocalDateTime.now());
                },
                () -> userDeviceRepository.save(
                        UserDevice.builder().user(user).token(token).build()
                )
        );
        log.info("기기 토큰 등록 완료. userId = {}", userId);
    }

    @Transactional
    public void deleteFcmToken(Long userId, String token) {
        if(token == null || token.isBlank()){
            throw new IllegalArgumentException("삭제할 FCM 토큰이 비어 있습니다.");
        }

        userDeviceRepository.deleteByUserIdAndToken(userId, token);

        log.info("기기 토큰 삭제 완료. userId = {}", userId);
    }
}