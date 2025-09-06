package com.example.bankcards.service;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.filter.CardFilter;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface CardService {

    ApiResponse addCard(CardDto.CreateCardDto dto);

    ApiResponse getCard(Long cardId);

    ApiResponse updateCard(Long cardId, CardDto.UpdateCardDto dto);

    ApiResponse deleteCard(Long cardId);

    ApiResponse getMyCards();

    ApiResponse getCardDetails(Long id);

    ApiResponse fillCard(String card, BigDecimal amount);

    ApiResponse transferMoneyBetweenCards(String from, String to, BigDecimal amount);

    ApiResponse changeCardStatus(Long id, CardStatus cardStatus);

    ApiResponse getAllCards(CardFilter filter, Pageable pageable);

    ApiResponse getBalance(Long id);
}
