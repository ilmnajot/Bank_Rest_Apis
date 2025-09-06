package com.example.bankcards.entity;

import com.example.bankcards.entity.base.BaseEntity;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "cars")
public class Card extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String encryptedNumber;

    private String lastFourDigits;

    @Column(nullable = false)
    private String ownerName;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private LocalDate expiryDate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner; // karta kimga tegishli

    private BigDecimal balance = BigDecimal.ZERO;
}
