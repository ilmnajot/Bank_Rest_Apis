package com.example.bankcards.service.impl;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.mapper.CardMapper;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transaction;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.entity.enums.TransactionType;
import com.example.bankcards.exception.CardBlockedException;
import com.example.bankcards.exception.CardExpiredException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.filter.CardFilter;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransactionRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.EncryptionService;
import com.example.bankcards.util.RestConstants;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public CardServiceImpl(
            CardRepository cardRepository,
            EncryptionService encryptionService,
            CardMapper cardMapper,
            UserRepository userRepository, TransactionRepository transactionRepository) {
        this.cardRepository = cardRepository;
        this.encryptionService = encryptionService;
        this.cardMapper = cardMapper;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public ApiResponse addCard(CardDto.CreateCardDto dto) {
        String cardNumber = this.encryptionService.generateCardNumber();
        String encrypt = this.encryptionService.encrypt(cardNumber);
        String maskedNumber = this.encryptionService.maskNumber(cardNumber);

        User owner = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + dto.getUserId()));

        if (this.checkCardExisting(encrypt)) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("This card has already been created!")
                    .build();
        }
        Card card = this.cardMapper.toEntity(dto, encrypt, maskedNumber, owner);
        Card saved = this.cardRepository.save(card);
        CardDto cardDto = this.cardMapper.toDto(saved);
        return ApiResponse.builder()
                .status(HttpStatus.CREATED)
                .message("Card has been successfully created!")
                .data(cardDto)
                .build();
    }

    private boolean checkCardExisting(String encrypt) {
        return this.cardRepository.existsByEncryptedNumber(encrypt);
    }

    @Override
    public ApiResponse getCard(Long cardId) {
        Card card = this.cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + cardId));
        CardDto cardDto = this.cardMapper.toDto(card);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message(RestConstants.SUCCESS)
                .data(cardDto)
                .build();
    }

    @Override
    public ApiResponse updateCard(Long cardId, CardDto.UpdateCardDto dto) {
        Card card = this.cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + cardId));
        this.cardMapper.toUpdate(dto, card);
        this.cardRepository.save(card);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("The the card has been updated successfully!l")
                .build();
    }

    @Override
    public ApiResponse deleteCard(Long cardId) {
        Card card = this.cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + cardId));
        card.setDeleted(true);
        this.cardRepository.save(card);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Card has been successfully deleted!")
                .build();
    }

    @Override
    public ApiResponse getMyCards() {
        User owner = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        if (owner == null) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("To see own cards, please login to the system!")
                    .build();
        }
        List<Card> cardList = this.cardRepository.findAllByOwnerId(owner.getId());

        if (cardList.isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.NOT_FOUND)
                    .data(new ArrayList<>())
                    .build();
        }
        List<CardDto> dtoList = this.cardMapper.toDto(cardList);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message(RestConstants.SUCCESS)
                .data(dtoList)
                .build();
    }

    @Override
    public ApiResponse getCardDetails(Long id) {
        Card card = this.cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + id));
        String decrypt = this.encryptionService.decrypt(card.getEncryptedNumber());
        CardDto.CardDetailsDto cardDto = this.cardMapper.toDto(card, decrypt);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message(RestConstants.SUCCESS)
                .data(cardDto)
                .build();
    }

    @Transactional
    @Override
    public ApiResponse fillCard(String cardNumber, BigDecimal amount) {
        if (this.checkAmount(amount)) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Card cannot be negative or zero")
                    .build();
        }
        String encrypt = this.encryptionService.encrypt(cardNumber);
        Card card = this.cardRepository.findByEncryptedNumber(encrypt)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        //check if the card is expired or blocked!
        this.validateCardForTransaction(card);

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardNotFoundException("Cannot fill inactive or blocked card");
        }

        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            throw new CardNotFoundException("Cannot fill inactive or blocked card");
        }
        card.setBalance(card.getBalance().add(amount));
        card = this.cardRepository.save(card);
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setCard(card);
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setDescription("Card fill operation!");
        transactionRepository.save(transaction);

        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Card successfully filled with " + amount)
                .build();
    }

    @Transactional
    @Override
    public ApiResponse transferMoneyBetweenCards(String from, String to, BigDecimal amount) {
        String encryptFrom = this.encryptionService.encrypt(from);
        String encryptTo = this.encryptionService.encrypt(to);

        Card cardFrom = this.cardRepository.findByEncryptedNumber(encryptFrom)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + from));

        Card cardTo = this.cardRepository.findByEncryptedNumber(encryptTo)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + to));

        //to check if the cards are not expired or blocked!
        this.validateCardForTransaction(cardFrom);
        this.validateCardForTransaction(cardTo);


        // Check if transferring to same card
        if (cardFrom.getId().equals(cardTo.getId())) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }


        if (this.checkAmount(amount)) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Card cannot be negative or zero")
                    .build();
        }

        if (cardFrom.getBalance().compareTo(amount) < 0) {
            return ApiResponse.builder()
                    .status(HttpStatus.BAD_REQUEST)
                    .message("Money is not enough to transfer!")
                    .build();
        }
        cardFrom.setBalance(cardFrom.getBalance().subtract(amount));
        cardTo.setBalance(cardTo.getBalance().add(amount));
        this.cardRepository.saveAll(List.of(cardFrom, cardTo));

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setDescription("Transfer to card: " + cardTo.getLastFourDigits());
        transaction.setCard(cardFrom);
        this.transactionRepository.save(transaction);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Money has successfully transferred!")
                .build();
    }

    @Override
    public ApiResponse changeCardStatus(Long id, CardStatus cardStatus) {
        Card card = this.cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + id));
        card.setStatus(cardStatus);
        this.cardRepository.save(card);
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message("Card successfully changed!")
                .build();
    }

    @Override
    public ApiResponse getAllCards(CardFilter filter, Pageable pageable) {
        Page<Card> cardPage = this.cardRepository.findAll(filter, pageable);
        if (cardPage.isEmpty()) {
            return ApiResponse.builder()
                    .status(HttpStatus.OK)
                    .data(new ArrayList<>())
                    .build();
        }
        List<CardDto> dtoList = this.cardMapper.toDto(cardPage.getContent());
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message(RestConstants.SUCCESS)
                .data(dtoList)
                .pages(cardPage.getTotalPages())
                .elements(cardPage.getTotalElements())
                .build();
    }

    @Override
    public ApiResponse getBalance(Long id) {
        Card card = this.cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found with ID: " + id));
        return ApiResponse.builder()
                .status(HttpStatus.OK)
                .message(RestConstants.SUCCESS)
                .data(card.getBalance())
                .build();
    }

    //every night to check if the card is not expired!
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkCard() {
        LocalDate today = LocalDate.now();
        List<Card> cardList = this.cardRepository.findAllByExpiryDateBeforeAndStatusNot(today, CardStatus.EXPIRED);
        if (cardList.isEmpty()) return;
        for (Card card : cardList) {
            card.setStatus(CardStatus.EXPIRED);
            this.cardRepository.save(card);
        }

    }

    //check if the amount is negative
    private boolean checkAmount(BigDecimal amount) {
        return amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    public void validateCardForTransaction(Card card) {
        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new CardExpiredException("Card is expired");
        }
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new CardBlockedException("Card is blocked");
        }
        if (card.getExpiryDate().isBefore(LocalDate.now())) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
            throw new CardExpiredException("Card expired on " + card.getExpiryDate());
        }
    }

}


