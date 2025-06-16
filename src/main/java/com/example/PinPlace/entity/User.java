package com.example.PinPlace.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue
    private Long id;

    private String loginId;
    private String password;
    private String nickname;
    private String image;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private final List<BookmarkList> bookmarkList = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private final List<Like> likes = new ArrayList<>();
    @OneToMany(mappedBy = "userToken", cascade = CascadeType.ALL)
    private final List<RefreshToken> refreshTokens = new ArrayList<>();

}
