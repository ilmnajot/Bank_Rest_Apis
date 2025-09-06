package com.example.bankcards.controller;

import com.example.bankcards.dto.ApiResponse;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.filter.CardFilter;
import com.example.bankcards.service.CardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cars")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/add-card")
    public ApiResponse addCard(@RequestBody CardDto.CreateCardDto dto) {
        return this.cardService.addCard(dto);
    }

    @GetMapping("/get/{id}")
    public ApiResponse getCard(@PathVariable Long id) {
        return this.cardService.getCard(id);
    }

    @GetMapping("/get-own-cards")
    public ApiResponse getMyCards() {
        return this.cardService.getMyCards();
    }

    @GetMapping("/get-card-details/{cardId}")
    public ApiResponse getCardDetails(@PathVariable(name = "cardId") Long id) {
        return this.cardService.getCardDetails(id);
    }

    @PostMapping("/fill-card")
    public ApiResponse fillCard(@RequestParam(value = "card") String card,
                                @RequestParam(value = "amount") BigDecimal amount) {
        return this.cardService.fillCard(card, amount);
    }

    @PostMapping("/transfer-money")
    public ApiResponse transferMoneyBetweenCards(@RequestParam(value = "cardFrom") String from,
                                                 @RequestParam(value = "cardTo") String to,
                                                 @RequestParam(value = "amount") BigDecimal amount) {
        return this.cardService.transferMoneyBetweenCards(from, to, amount);
    }

    @PostMapping("/change-card-status/{cardId}")
    public ApiResponse changeCardStatus(@PathVariable(value = "cardId") Long id,
                                        @RequestParam(value = "cardStatus") CardStatus cardStatus) {
        return this.cardService.changeCardStatus(id, cardStatus);
    }

    @GetMapping("/get-all-cards")
    public ApiResponse getAllCards(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) CardStatus status,
            @RequestParam(value = "deleted", required = false, defaultValue = "false") Boolean deleted,
            @RequestParam(value = "int", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        CardFilter filter = new CardFilter();
        filter.setKeyword(keyword);
        filter.setStatus(status);
        filter.setDeleted(deleted);
        return this.cardService.getAllCards(filter, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @GetMapping("/get-balance")
    public ApiResponse getBalance(@RequestParam(value = "cardId") Long id) {
        return this.cardService.getBalance(id);
    }

    @DeleteMapping("/delete-card/{cardId}")
    public ApiResponse deleteCard(@PathVariable(name = "cardId") Long cardId) {
        return this.cardService.deleteCard(cardId);
    }

    @PutMapping("/update-card/{cardId}")
    public ApiResponse updateCard(@PathVariable(value = "cardId") Long id, @RequestBody CardDto.UpdateCardDto dto) {
        return this.cardService.updateCard(id, dto);
    }

}
