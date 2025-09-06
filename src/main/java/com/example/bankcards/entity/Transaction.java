package com.example.bankcards.entity;

import com.example.bankcards.entity.base.BaseEntity;
import com.example.bankcards.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "transactions")
public class Transaction extends BaseEntity {

    private String description;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    private BigDecimal amount;
    @ManyToOne
    private Card card;
}
