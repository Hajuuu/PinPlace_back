package com.example.PinPlace.dto;

import com.example.PinPlace.entity.Role;
import com.example.PinPlace.entity.User;
import com.example.PinPlace.valid.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequestDTO {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 6, max = 20, message = "아이디는 6 ~ 20자입니다.")
    private String loginId;

    @ValidPassword
    private String password;
    private String nickname;

    public User toEntity(String encodedPassword) {
        return User.builder()
                .loginId(loginId)
                .password(encodedPassword)
                .nickname(nickname)
                .role(Role.USER)
                .build();
    }
}
