package com.example.PinPlace.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Place {

    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String address;
    private String latitude;
    private String longitude;
    private String phone;
    private String url;
    private String category;
    private String category_detail;
}
