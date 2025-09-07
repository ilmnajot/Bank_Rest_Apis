package com.example.bankcards.service;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.mapper.CardMapper;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.entity.enums.TransactionType;
import com.example.bankcards.entity.enums.UserStatus;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardExpiredException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.filter.CardFilter;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.impl.CardServiceImpl;
import com.example.bankcards.util.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @InjectMocks
    private CardServiceImpl cardService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User testUser;
    private Card testCard;
    private Card testCard2;
    private CardDto.CreateCardDto createCardDto;
    private CardDto expectedCardDto;

    @BeforeEach
    void setUp() {
        // Test user
        testUser = User.builder()
                .fullName("John Doe")
                .username("johndoe")
                .password("password123")
                .role(Role.USER)
                .userStatus(UserStatus.ACTIVE)
                .build();
        testUser.setId(1L);

        // Create card DTO
        createCardDto = new CardDto.CreateCardDto();
        createCardDto.setUserId(1L);

        // Test cards
        testCard = Card.builder()
                .owner(testUser)
                .encryptedNumber("ENCRYPTED_CARD_1")
                .lastFourDigits("**** **** **** 1234")
                .ownerName("John Doe")
                .status(CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(5))
                .balance(BigDecimal.valueOf(1000))
                .build();
        testCard.setId(1L);

        testCard2 = Card.builder()
                .owner(testUser)
                .encryptedNumber("ENCRYPTED_CARD_2")
                .lastFourDigits("**** **** **** 5678")
                .ownerName("John Doe")
                .status(CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(5))
                .balance(BigDecimal.valueOf(500))
                .build();
        testCard2.setId(2L);

        // Expected DTO
        expectedCardDto = CardDto.builder()
                .id(1L)
                .maskedNumber("**** **** **** 1234")
                .ownerName("John Doe")
                .status(CardStatus.ACTIVE)
                .expiryDate(LocalDate.now().plusYears(5))
                .balance(BigDecimal.valueOf(1000))
                .build();

        // Security context setup
        SecurityContextHolder.setContext(securityContext);
    }

    // ============= CREATE CARD TESTS =============

    @Test
    @DisplayName("Should create card successfully when user exists")
    void testAddCard_Success() {
        // Given
        String generatedCardNumber = "1234567890123456";
        String encryptedNumber = "ENCRYPTED_CARD_NUMBER";
        String maskedNumber = "**** **** **** 3456";

        when(encryptionService.generateCardNumber()).thenReturn(generatedCardNumber);
        when(encryptionService.encrypt(generatedCardNumber)).thenReturn(encryptedNumber);
        when(encryptionService.maskNumber(generatedCardNumber)).thenReturn(maskedNumber);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.existsByEncryptedNumber(encryptedNumber)).thenReturn(false);
        when(cardMapper.toEntity(createCardDto, encryptedNumber, maskedNumber, testUser)).thenReturn(testCard);
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(cardMapper.toDto(testCard)).thenReturn(expectedCardDto);

        // When
        ApiResponse response = cardService.addCard(createCardDto);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertEquals("Card has been successfully created!", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when card already exists")
    void testAddCard_CardAlreadyExists() {
        // Given
        String generatedCardNumber = "1234567890123456";
        String encryptedNumber = "ENCRYPTED_CARD_NUMBER";

        when(encryptionService.generateCardNumber()).thenReturn(generatedCardNumber);
        when(encryptionService.encrypt(generatedCardNumber)).thenReturn(encryptedNumber);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cardRepository.existsByEncryptedNumber(encryptedNumber)).thenReturn(true);

        // When
        ApiResponse response = cardService.addCard(createCardDto);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("This card has already been created!", response.getMessage());
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void testAddCard_UserNotFound() {
        // Given
        when(encryptionService.generateCardNumber()).thenReturn("1234567890123456");
        when(encryptionService.encrypt(anyString())).thenReturn("ENCRYPTED");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> cardService.addCard(createCardDto));
    }

    // ============= GET CARD TESTS =============

    @Test
    @DisplayName("Should get card successfully when card exists")
    void testGetCard_Success() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardMapper.toDto(testCard)).thenReturn(expectedCardDto);

        // When
        ApiResponse response = cardService.getCard(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getData());
    }

    @Test
    @DisplayName("Should throw CardNotFoundException when card does not exist")
    void testGetCard_CardNotFound() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CardNotFoundException.class, () -> cardService.getCard(1L));
    }

    // ============= TRANSFER MONEY TESTS =============

    @Test
    @DisplayName("Should transfer money successfully between valid cards")
    void testTransferMoney_Success() {
        // Given
        String fromCardNumber = "1111222233334444";
        String toCardNumber = "5555666677778888";
        String encryptedFrom = "ENCRYPTED_FROM";
        String encryptedTo = "ENCRYPTED_TO";
        BigDecimal transferAmount = BigDecimal.valueOf(100);

        when(encryptionService.encrypt(fromCardNumber)).thenReturn(encryptedFrom);
        when(encryptionService.encrypt(toCardNumber)).thenReturn(encryptedTo);
        when(cardRepository.findByEncryptedNumber(encryptedFrom)).thenReturn(Optional.of(testCard));
        when(cardRepository.findByEncryptedNumber(encryptedTo)).thenReturn(Optional.of(testCard2));
        when(cardRepository.saveAll(anyList())).thenReturn(Arrays.asList(testCard, testCard2));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        ApiResponse response = cardService.transferMoneyBetweenCards(fromCardNumber, toCardNumber, transferAmount);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Money has successfully transferred!", response.getMessage());
        assertEquals(BigDecimal.valueOf(900), testCard.getBalance()); // 1000 - 100
        assertEquals(BigDecimal.valueOf(600), testCard2.getBalance()); // 500 + 100
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when insufficient balance")
    void testTransferMoney_InsufficientBalance() {
        // Given
        String fromCardNumber = "1111222233334444";
        String toCardNumber = "5555666677778888";
        String encryptedFrom = "ENCRYPTED_FROM";
        String encryptedTo = "ENCRYPTED_TO";
        BigDecimal transferAmount = BigDecimal.valueOf(2000); // More than balance

        when(encryptionService.encrypt(fromCardNumber)).thenReturn(encryptedFrom);
        when(encryptionService.encrypt(toCardNumber)).thenReturn(encryptedTo);
        when(cardRepository.findByEncryptedNumber(encryptedFrom)).thenReturn(Optional.of(testCard));
        when(cardRepository.findByEncryptedNumber(encryptedTo)).thenReturn(Optional.of(testCard2));

        // When
        ApiResponse response = cardService.transferMoneyBetweenCards(fromCardNumber, toCardNumber, transferAmount);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Money is not enough to transfer!", response.getMessage());
        verify(cardRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Should throw exception when transferring to same card")
    void testTransferMoney_SameCard() {
        // Given
        String cardNumber = "1111222233334444";
        String encrypted = "ENCRYPTED_CARD";
        BigDecimal transferAmount = BigDecimal.valueOf(100);

        when(encryptionService.encrypt(cardNumber)).thenReturn(encrypted);
        when(cardRepository.findByEncryptedNumber(encrypted)).thenReturn(Optional.of(testCard));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> cardService.transferMoneyBetweenCards(cardNumber, cardNumber, transferAmount));
    }

    @Test
    @DisplayName("Should throw exception when card is blocked")
    void testTransferMoney_BlockedCard() {
        // Given
        String fromCardNumber = "1111222233334444";
        String toCardNumber = "5555666677778888";
        String encryptedFrom = "ENCRYPTED_FROM";
        String encryptedTo = "ENCRYPTED_TO";
        BigDecimal transferAmount = BigDecimal.valueOf(100);

        Card blockedCard = Card.builder()
//                .id(1L)
                .status(CardStatus.BLOCKED)
                .balance(BigDecimal.valueOf(1000))
                .build();

        when(encryptionService.encrypt(fromCardNumber)).thenReturn(encryptedFrom);
        when(encryptionService.encrypt(toCardNumber)).thenReturn(encryptedTo);
        when(cardRepository.findByEncryptedNumber(encryptedFrom)).thenReturn(Optional.of(blockedCard));
        when(cardRepository.findByEncryptedNumber(encryptedTo)).thenReturn(Optional.of(testCard2));

        // When & Then
        assertThrows(CardBlockedException.class,
                () -> cardService.transferMoneyBetweenCards(fromCardNumber, toCardNumber, transferAmount));
    }

    @Test
    @DisplayName("Should throw exception when card is expired")
    void testTransferMoney_ExpiredCard() {
        // Given
        String fromCardNumber = "1111222233334444";
        String toCardNumber = "5555666677778888";
        String encryptedFrom = "ENCRYPTED_FROM";
        String encryptedTo = "ENCRYPTED_TO";
        BigDecimal transferAmount = BigDecimal.valueOf(100);

        Card expiredCard = Card.builder()
//                .id(1L)
                .status(CardStatus.EXPIRED)
                .expiryDate(LocalDate.now().minusDays(1))
                .balance(BigDecimal.valueOf(1000))
                .build();
        expiredCard.setId(1L);

        when(encryptionService.encrypt(fromCardNumber)).thenReturn(encryptedFrom);
        when(encryptionService.encrypt(toCardNumber)).thenReturn(encryptedTo);
        when(cardRepository.findByEncryptedNumber(encryptedFrom)).thenReturn(Optional.of(expiredCard));
        when(cardRepository.findByEncryptedNumber(encryptedTo)).thenReturn(Optional.of(testCard2));

        // When & Then
        assertThrows(CardExpiredException.class,
                () -> cardService.transferMoneyBetweenCards(fromCardNumber, toCardNumber, transferAmount));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for negative transfer amount")
    void testTransferMoney_NegativeAmount() {
        // Given
        String fromCardNumber = "1111222233334444";
        String toCardNumber = "5555666677778888";
        BigDecimal negativeAmount = BigDecimal.valueOf(-100);

        // When
        ApiResponse response = cardService.transferMoneyBetweenCards(fromCardNumber, toCardNumber, negativeAmount);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Card cannot be negative or zero", response.getMessage());

        // Verify that encryption methods are not called for negative amounts
        verify(encryptionService, never()).encrypt(anyString());
        verify(cardRepository, never()).findByEncryptedNumber(anyString());
    }

    // ============= FILL CARD TESTS =============

    @Test
    @DisplayName("Should fill card successfully")
    void testFillCard_Success() {
        // Given
        String cardNumber = "1111222233334444";
        String encrypted = "ENCRYPTED_CARD";
        BigDecimal fillAmount = BigDecimal.valueOf(500);

        when(encryptionService.encrypt(cardNumber)).thenReturn(encrypted);
        when(cardRepository.findByEncryptedNumber(encrypted)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(testCard)).thenReturn(testCard);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());

        // When
        ApiResponse response = cardService.fillCard(cardNumber, fillAmount);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(response.getMessage().contains("Card successfully filled with"));
        assertEquals(BigDecimal.valueOf(1500), testCard.getBalance()); // 1000 + 500
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should return BAD_REQUEST for negative fill amount")
    void testFillCard_NegativeAmount() {
        // Given
        String cardNumber = "1111222233334444";
        BigDecimal negativeAmount = BigDecimal.valueOf(-100);

        // When
        ApiResponse response = cardService.fillCard(cardNumber, negativeAmount);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("Card cannot be negative or zero", response.getMessage());
    }

    // ============= GET MY CARDS TESTS =============

    @Test
    @DisplayName("Should get user's cards when authenticated")
    void testGetMyCards_Success() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(cardRepository.findAllByOwnerId(testUser.getId())).thenReturn(Arrays.asList(testCard, testCard2));
        when(cardMapper.toDto(anyList())).thenReturn(Arrays.asList(expectedCardDto));

        // When
        ApiResponse response = cardService.getMyCards();

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getData());
        assertTrue(response.getData() instanceof List);
    }

    @Test
    @DisplayName("Should return BAD_REQUEST when user not authenticated")
    void testGetMyCards_NotAuthenticated() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(null);

        // When
        ApiResponse response = cardService.getMyCards();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatus());
        assertEquals("To see own cards, please login to the system!", response.getMessage());
    }

    @Test
    @DisplayName("Should return empty list when user has no cards")
    void testGetMyCards_EmptyList() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(cardRepository.findAllByOwnerId(testUser.getId())).thenReturn(Collections.emptyList());

        // When
        ApiResponse response = cardService.getMyCards();

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        assertTrue(response.getData() instanceof List);
        assertTrue(((List<?>) response.getData()).isEmpty());
    }

    // ============= CHANGE CARD STATUS TESTS =============

    @Test
    @DisplayName("Should change card status successfully")
    void testChangeCardStatus_Success() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(testCard)).thenReturn(testCard);

        // When
        ApiResponse response = cardService.changeCardStatus(1L, CardStatus.BLOCKED);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Card successfully changed!", response.getMessage());
        assertEquals(CardStatus.BLOCKED, testCard.getStatus());
    }

    // ============= GET ALL CARDS TESTS =============

    @Test
    @DisplayName("Should get all cards with pagination")
    void testGetAllCards_Success() {
        // Given
        CardFilter filter = new CardFilter();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(Arrays.asList(testCard, testCard2), pageable, 2);

        when(cardRepository.findAll(filter, pageable)).thenReturn(cardPage);
        when(cardMapper.toDto(anyList())).thenReturn(Arrays.asList(expectedCardDto));

        // When
        ApiResponse response = cardService.getAllCards(filter, pageable);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getData());
        assertEquals(1, response.getPages());
        assertEquals(2L, response.getElements());
    }

    // ============= GET BALANCE TESTS =============

    @Test
    @DisplayName("Should get card balance successfully")
    void testGetBalance_Success() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));

        // When
        ApiResponse response = cardService.getBalance(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(testCard.getBalance(), response.getData());
    }

    // ============= DELETE CARD TESTS =============

    @Test
    @DisplayName("Should delete card successfully")
    void testDeleteCard_Success() {
        // Given
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(testCard)).thenReturn(testCard);

        // When
        ApiResponse response = cardService.deleteCard(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Card has been successfully deleted!", response.getMessage());
        assertTrue(testCard.getDeleted());
    }

    // ============= UPDATE CARD TESTS =============

    @Test
    @DisplayName("Should update card successfully")
    void testUpdateCard_Success() {
        // Given
        CardDto.UpdateCardDto updateDto = new CardDto.UpdateCardDto();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(testCard)).thenReturn(testCard);
        doNothing().when(cardMapper).toUpdate(updateDto, testCard);

        // When
        ApiResponse response = cardService.updateCard(1L, updateDto);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(response.getMessage().contains("updated successfully"));
        verify(cardMapper).toUpdate(updateDto, testCard);
    }
}