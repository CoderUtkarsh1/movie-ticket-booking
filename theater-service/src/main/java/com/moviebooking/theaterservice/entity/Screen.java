package com.moviebooking.theaterservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moviebooking.common.enums.ScreenType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "screens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Screen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Theater theater;

    @Column(nullable = false)
    private String screenName;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ScreenType screenType = ScreenType.TWO_D;

    private Integer totalSeats;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    private List<Seat> seats = new ArrayList<>();
}
