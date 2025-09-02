package com.example.bankcards.entity;

import com.example.bankcards.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "cars")
public class Card extends BaseEntity {

    private String name;
    private LocalDate expireDate;
    private int cardNumber;
    private Double balance;
    @Enumerated(EnumType.STRING)
    private String CardStatus;
    @ManyToOne
    private User user;
}
