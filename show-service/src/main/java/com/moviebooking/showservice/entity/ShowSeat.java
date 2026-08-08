package com.moviebooking.showservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moviebooking.common.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "show_seats")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShowSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private Show show;

    @Column(nullable = false)
    private Long seatId;  // References Theater Service Seat

    @Column(nullable = false, length = 5)
    private String seatRow;

    @Column(nullable = false)
    private Integer seatNumber;

    private String seatType;  // REGULAR, PREMIUM, VIP

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;
}
