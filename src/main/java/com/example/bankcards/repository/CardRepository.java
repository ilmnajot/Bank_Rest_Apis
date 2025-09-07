package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card,Long>, JpaSpecificationExecutor<Card> {
    boolean existsByEncryptedNumber(String encryptedNumber);

    List<Card> findAllByOwnerId(Long id);


    Optional<Card> findByEncryptedNumber(String encryptedNumber);

    List<Card> findAllByExpiryDateBeforeAndStatusNot(LocalDate expiryDateBefore, CardStatus status);
}
