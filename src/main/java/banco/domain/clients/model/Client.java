package banco.domain.clients.model;

import banco.domain.cards.model.BankCard;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Client {
    private Long id;
    private String name;
    private String username;
    private String email;
    private List<BankCard> card;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Client (String name, String username, String email){
        this.id = null;
        this.name = name;
        this.username = username;
        this.email = email;
        this.card = null;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
