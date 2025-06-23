package com.example.PinPlace.service;

import com.example.PinPlace.dto.SignupRequestDTO;
import com.example.PinPlace.dto.UpdateUserRequestDTO;
import com.example.PinPlace.entity.User;
import com.example.PinPlace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileService fileService;

    public void signup(SignupRequestDTO request) {
        if (userRepository.existsByLoginId(request.getLoginId())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = request.toEntity(encodedPassword);
        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long userId, UpdateUserRequestDTO userRequestDTO, MultipartFile profileImage) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (userRequestDTO.isDeleteProfileImage() && user.getProfileImage() != null) {
            fileService.deleteFile(user.getProfileImage());
            user.removeProfileImage();
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            if (user.getProfileImage() != null) {
                fileService.deleteFile(user.getProfileImage());
            }
            String imageUrl = fileService.saveProfileImage(profileImage);
            user.updateProfileImage(imageUrl);
        }

        if (userRequestDTO.getNickname() != null && !userRequestDTO.getNickname().equals(user.getNickname())) {
            user.changeNickname(userRequestDTO.getNickname());
        }

        if (userRequestDTO.getNewPassword() != null && !userRequestDTO.getNewPassword().isBlank()) {
            if (userRequestDTO.getCurrentPassword() != null && !passwordEncoder.matches(userRequestDTO.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }
            if (!userRequestDTO.getNewPassword().equals(userRequestDTO.getConfirmPassword())) {
                throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            }
            String encodedPassword = passwordEncoder.encode(userRequestDTO.getNewPassword());
            user.changePassword(encodedPassword);
        }
        userRepository.save(user);
    }
}
