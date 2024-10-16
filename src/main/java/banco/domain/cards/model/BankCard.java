package banco.domain.cards.model;

import banco.domain.clients.model.Client;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BankCard {
    // Temporal el uuid como clave primaria podria ser el numero de tarjeta
    private UUID uuid;
    private String number;
    private Client client;
    private LocalDate expirationDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
