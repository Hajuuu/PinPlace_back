package com.example.PinPlace.dto.kakao;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class KakaoInfo {

    String id;
    String place_name;
    String category_name;
    String category_group_code;
    String category_group_name;
    String phone;
    String address_name;
    String road_address_name;
    String x;
    String y;
    String place_url;
    String distance;
}
