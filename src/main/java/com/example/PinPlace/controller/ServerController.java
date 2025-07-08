package com.example.PinPlace.controller;

import com.example.PinPlace.dto.kakao.KakaoDTO;
import com.example.PinPlace.service.KakaoSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ServerController {


    private final KakaoSearchService kakaoSearchService;


    @GetMapping("/search")
    public KakaoDTO kakaoSearch(@RequestParam(name = "name", required = false) String name) {
        return kakaoSearchService.getRestaurantInfo(name);
    }
}
