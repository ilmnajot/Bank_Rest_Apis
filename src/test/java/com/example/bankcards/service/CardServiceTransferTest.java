package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CardServiceTransferTest {

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionService encryptionService;

    private Card cardFrom;
    private Card cardTo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User();
        user.setId(1L);

        cardFrom = new Card();
        cardFrom.setId(1L);
        cardFrom.setBalance(new BigDecimal("500"));
        cardFrom.setStatus(CardStatus.ACTIVE);
        cardFrom.setOwner(user);
        cardFrom.setEncryptedNumber("encryptedFrom");
        cardFrom.setLastFourDigits("1111");
        cardFrom.setExpiryDate(LocalDate.now());


        cardTo = new Card();
        cardTo.setId(2L);
        cardTo.setBalance(BigDecimal.ZERO);
        cardTo.setStatus(CardStatus.ACTIVE);
        cardTo.setOwner(user);
        cardTo.setEncryptedNumber("encryptedTo");
        cardTo.setLastFourDigits("2222");
        cardTo.setExpiryDate(LocalDate.now());
    }

    @Test
    void testTransferMoney_Success() {
        String from = "1111";
        String to = "2222";
        BigDecimal amount = new BigDecimal("100");

        when(encryptionService.encrypt(from)).thenReturn("encryptedFrom");
        when(encryptionService.encrypt(to)).thenReturn("encryptedTo");

        when(cardRepository.findByEncryptedNumber("encryptedFrom")).thenReturn(Optional.of(cardFrom));
        when(cardRepository.findByEncryptedNumber("encryptedTo")).thenReturn(Optional.of(cardTo));

        when(cardRepository.saveAll(any())).thenReturn(null);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        var response = cardService.transferMoneyBetweenCards(from, to, amount);

        assertEquals("Money has successfully transferred!", response.getMessage());
        assertEquals(new BigDecimal("400"), cardFrom.getBalance());
        assertEquals(new BigDecimal("100"), cardTo.getBalance());

        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(cardRepository).saveAll(any());
    }
}
