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
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CardServiceTransactionTest {

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

    private Card testCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        User user = new User();
        user.setId(1L);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setBalance(BigDecimal.ZERO);
        testCard.setStatus(CardStatus.ACTIVE);
        testCard.setOwner(user);
        testCard.setEncryptedNumber("encrypted123");
        testCard.setExpiryDate(LocalDate.now());

    }

    @Test
    void testFillCard_Success() {
        String cardNumber = "1234567890123456";
        BigDecimal amount = new BigDecimal("100");

        when(encryptionService.encrypt(cardNumber)).thenReturn("encrypted123");
        when(cardRepository.findByEncryptedNumber("encrypted123")).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        var response = cardService.fillCard(cardNumber, amount);

        assertEquals("Card successfully filled with 100", response.getMessage());
        assertEquals(BigDecimal.valueOf(100), testCard.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}
