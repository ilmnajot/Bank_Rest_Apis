package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Builder
@Data
public class CardDto {

    private Long id;

    private String maskedNumber;
    private String ownerName;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private String cardNumber;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private Long updatedBy;
    private Boolean deleted;


    @Data
    public static class CreateCardDto {

        @NotNull(message = "User ID is required")
        private Long userId;


    }

    @Data
    public static class UpdateCardDto {

        @Size(min = 2, max = 100, message = "Card holder name must be between 2 and 100 characters")
        private String cardHolder;

        @Future(message = "Expiry date must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDate expiryDate;

        private CardStatus status;
    }
    @Builder
    @Data
    public static class CardDetailsDto {

        private Long id;

        private String cardNumber;
        private String ownerName;
        private LocalDate expiryDate;
        private CardStatus status;
        private BigDecimal balance;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Long createdBy;
        private Long updatedBy;
        private Boolean deleted;
    }


}
