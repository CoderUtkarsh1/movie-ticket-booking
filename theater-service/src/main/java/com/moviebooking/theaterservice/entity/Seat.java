package com.moviebooking.theaterservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moviebooking.common.enums.SeatType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "seats")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Screen screen;

    @Column(nullable = false, length = 5)
    private String seatRow;      // A, B, C, D...

    @Column(nullable = false)
    private Integer seatNumber;  // 1, 2, 3...

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SeatType seatType = SeatType.REGULAR;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;
}
