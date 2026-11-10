package com.springboot.roadready.model;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "location")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city", length = 60, nullable = false)
    private String city;

    @Column(name = "state", length = 60, nullable = false)
    private String state;

    @Column(name = "address", length = 255)
    private String address;
}