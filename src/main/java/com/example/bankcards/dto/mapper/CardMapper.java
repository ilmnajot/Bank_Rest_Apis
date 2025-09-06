package com.example.bankcards.dto.mapper;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.util.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CardMapper {

    public CardDto toDto(Card card) {
        return CardDto.builder()
                .id(card.getId())
                .maskedNumber(card.getLastFourDigits())
                .ownerName(card.getOwnerName())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .createdBy(card.getCreatedBy())
                .updatedBy(card.getUpdatedBy())
                .deleted(card.getDeleted())
                .build();
    }

    public CardDto.CardDetailsDto toDto(Card card, String cardNumber) {
        return CardDto.CardDetailsDto.builder()
                .id(card.getId())
                .cardNumber(cardNumber)
                .ownerName(card.getOwnerName())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .createdBy(card.getCreatedBy())
                .updatedBy(card.getUpdatedBy())
                .deleted(card.getDeleted())
                .build();
    }

    public Card toEntity(CardDto.CreateCardDto dto, String encrypt, String masked, User owner) {
        if (dto == null) return null;
        return Card.builder()
                .owner(owner)
                .lastFourDigits(masked)
                .encryptedNumber(encrypt)
                .ownerName(owner.getFullName())
                .expiryDate(LocalDate.now().plusYears(5))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
    }

    public List<CardDto> toDto(List<Card> cardList) {
        if (cardList == null) {
            return new ArrayList<>();
        }
        return cardList
                .stream()
                .map(this::toDto)
                .toList();
    }

    public void toUpdate(CardDto.UpdateCardDto dto, Card card) {
        if (dto == null) return;
        if (dto.getCardHolder() != null && !dto.getCardHolder().isEmpty()) {
            card.setOwnerName(dto.getCardHolder());
        }
        if (dto.getExpiryDate() != null && !dto.getExpiryDate().isBefore(LocalDate.now())) {
            card.setExpiryDate(dto.getExpiryDate());
        }
        if (dto.getStatus() != null) {
            card.setStatus(dto.getStatus());
        }
    }

}
