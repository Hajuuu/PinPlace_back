package com.example.PinPlace.dto.kakao;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class KakaoDTO {
    private List<KakaoInfo> documents = new ArrayList<>();
}
