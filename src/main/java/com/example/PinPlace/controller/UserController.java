package com.example.PinPlace.controller;

import com.example.PinPlace.dto.UpdateUserRequestDTO;
import com.example.PinPlace.security.CustomUserDetails;
import com.example.PinPlace.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/pinplace/user/profile")
public class UserController {

    private UserService userService;

    @GetMapping("/")
    public ResponseEntity<String> getProfile() {
        return ResponseEntity.ok("User profile data");
    }

    @PostMapping("/update")
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestPart("data") @Valid UpdateUserRequestDTO userRequestDTO, @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        userService.updateUser(userDetails.getUser().getId(), userRequestDTO, profileImage);
        return ResponseEntity.ok("회원 정보가 수정되었습니다.");
    }
}
