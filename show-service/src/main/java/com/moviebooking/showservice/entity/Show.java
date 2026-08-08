package com.moviebooking.showservice.entity;

import com.moviebooking.common.enums.ShowStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shows")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Show {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private String movieId;  // References Movie Service (MongoDB ID)

    @Column(nullable = false)
    private Long screenId;   // References Theater Service

    @Column(nullable = false)
    private Long theaterId;  // References Theater Service

    @Column(nullable = false)
    private LocalDate showDate;

    @Column(nullable = false)
    private LocalTime showTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ShowStatus status = ShowStatus.OPEN;

    @OneToMany(mappedBy = "show", fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<ShowSeat> showSeats = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
