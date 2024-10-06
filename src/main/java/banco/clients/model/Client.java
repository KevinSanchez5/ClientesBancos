package banco.clients.model;

import banco.cards.model.BankCard;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Client {
    private UUID id;
    private String name;
    private String username;
    private String email;
    //Puede ser una lista si tiene varias tarjetas
    private BankCard card;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Client (String name, String username, String email){
        this.id = UUID.randomUUID();
        this.name = name;
        this.username = username;
        this.email = email;
        this.card = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
