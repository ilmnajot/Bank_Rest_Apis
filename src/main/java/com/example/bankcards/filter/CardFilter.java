package com.example.bankcards.filter;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class CardFilter implements Specification<Card> {
    private String keyword;
    private CardStatus status;
    private Boolean deleted;

    public void setKeyword(String keyword) {
        if (keyword != null) {
            this.keyword = keyword;
        }
    }


    public void setStatus(CardStatus status) {
        if (status != null) {
            this.status = status;
        }
    }

    public void setDeleted(Boolean deleted) {
        if (deleted != null) {
            this.deleted = deleted;
        }
    }

    @Override
    public Predicate toPredicate(@NonNull Root<Card> root,
                                 CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            Predicate keywordPredicate = cb.or(
                    cb.like(cb.lower(root.get("ownerName")), kw),
                    cb.like(cb.lower(root.get("lastFourDigits")), kw)
            );
            predicates.add(keywordPredicate);
        }

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        predicates.add(cb.equal(root.get("deleted"), deleted));

        query.orderBy(cb.desc(root.get("id")));
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
