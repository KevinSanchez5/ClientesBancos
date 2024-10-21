package banco.domain.clients.model;

import banco.domain.cards.model.BankCard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Client {
    private Long id;
    private String name;
    private String username;
    private String email;
    private List<BankCard> cards = Collections.emptyList();
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Client(Long id, String name, String username, String email) {
        this.id =  id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.cards = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}
