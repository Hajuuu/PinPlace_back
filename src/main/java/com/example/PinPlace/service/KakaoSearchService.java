package com.example.PinPlace.service;


import com.example.PinPlace.dto.kakao.KakaoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Service
public class KakaoSearchService {

    @Value("${KAKAO-KEY}")
    private String KAKAO_KEY;

    private static final String apiURL = "https://dapi.kakao.com/v2/local/search/keyword.json";

    public KakaoDTO getRestaurantInfo(String name) {
        String encodedName = encodingName(name);
        WebClient webClient = createWebClient();

        Mono<KakaoDTO> response = webClient.get().uri(uriBuilder -> uriBuilder
                        .queryParam("query", encodedName)
                        .queryParam("category_group_code", "FD6")
                        .queryParam("y", "37.54817")
                        .queryParam("x", "127.073403")
                        .queryParam("radius", 800)
                        .build()
                ).accept(MediaType.APPLICATION_JSON)
                .retrieve().bodyToMono(KakaoDTO.class);
        return response.block();
    }

    private WebClient createWebClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(apiURL);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        return WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(apiURL)
                .defaultHeader("Authorization", "KakaoAK " + KAKAO_KEY)
                .build();
    }

    private String encodingName(String name) {
        String encodedText = "";
        try {
            encodedText = URLEncoder.encode(name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("검색어 인코딩 실패", e);
        }
        return encodedText;
    }
}

