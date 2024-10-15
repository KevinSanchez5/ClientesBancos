package banco.domain.cards.model;

import banco.domain.clients.model.Client;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class BankCard {
    //clave primaria
    private String number;
    private Client client;
    private LocalDate expirationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
