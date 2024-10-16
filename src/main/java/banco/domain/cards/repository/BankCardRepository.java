package banco.domain.cards.repository;

import banco.domain.cards.model.BankCard;
import banco.util.Repository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface BankCardRepository extends Repository<String, BankCard> {
    public CompletableFuture<List<BankCard>> getBankCardsByClientId(Long client);
}
