package banco.clients.validator;

import banco.cards.model.BankCard;

import java.time.LocalDateTime;
import java.util.UUID;

public class ClientValidator {
    private UUID id;
    private String name;
    private String username;
    private String email;
    //Puede ser una lista si tiene varias tarjetas
    private BankCard card;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



}