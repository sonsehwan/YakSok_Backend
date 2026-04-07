package com.sehwan.YakSok.user.service;


import com.sehwan.YakSok.user.dto.*;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//비즈니스 로직을 처리하는 서비스
@Service

/**
 * @RequiredArgsConstructor
 * final 필드나 @NonNull 필드에 대해서만 생성자 매개변수를 만듭니다.
 * 1. 생성자를 만들려고 보니 UserRepository가 필요한 것을 확인
 * 2. 이미 자동 설정으로 만들어둔 UserRepository를 찾아와 생성자를 만든다.
 */
@RequiredArgsConstructor


@Transactional(readOnly = true)
public class UserService {

    //여기에 자동 생성된 빈을
    private final UserRepository userRepository;

    @Transactional
    public String signUp(UserRequest dto) {
        // 1. 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("이미 사용 중인 이메일입니다.");
        }

        if(userRepository.existsByNickname(dto.getNickname())) {
            throw  new RuntimeException("이미 존재하는 닉네임입니다.");
        }

        // 3. DTO를 엔티티로 변환하여 저장
        User user = dto.toEntity();
        System.out.println("디버깅: DB 저장 직전!");
        return userRepository.save(user).getEmail();
    }

    @Transactional
    public UserResponse login(LoginRequest dto){

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        if(!user.getPassword().equals(dto.getPassword())){
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        System.out.println("로그인 성공");

        return new UserResponse(user);
    }

    @Transactional
    public UserResponse modifyInfo(ModifyInfoRequest dto){
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(!user.getNickname().equals(dto.getNickname()) && userRepository.existsByNickname(dto.getNickname())){
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        user.setNickname(dto.getNickname());

        return new UserResponse(user);
    }

    @Transactional
    public void modifyPassword(ModifyPasswordRequest dto){
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(!user.getPassword().equals(dto.getCurrentPassword())){
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(dto.getNewPassword());
    }

}