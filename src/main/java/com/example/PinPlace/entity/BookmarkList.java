package com.example.PinPlace.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkList {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "bookmarkList", cascade = CascadeType.ALL)
    private List<Like> likes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User owner;

    private String title;
    private Long viewCount;
    private Integer icon;
}
