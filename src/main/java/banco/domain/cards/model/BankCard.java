package banco.domain.cards.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor

public class BankCard {
    private String number;
    private Long clientId;
    private LocalDate expirationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BankCard() {
    }

    public BankCard(String number, Long clientId, LocalDate expirationDate) {
        this.number = number;
        this.clientId = clientId;
        this.expirationDate = expirationDate;
    }
}
