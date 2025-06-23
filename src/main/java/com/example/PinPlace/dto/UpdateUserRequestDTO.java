package com.example.PinPlace.dto;

import com.example.PinPlace.valid.ValidPassword;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequestDTO {
    private String nickname;
    private String currentPassword;
    @ValidPassword
    private String newPassword;
    private String confirmPassword;
    private boolean deleteProfileImage;
}
