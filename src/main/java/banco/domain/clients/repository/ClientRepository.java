package banco.domain.clients.repository;

import banco.domain.cards.model.BankCard;
import banco.domain.clients.model.Client;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ClientRepository extends Repository<Long, Client> {

    public CompletableFuture<BankCard> saveBankCard(BankCard bankCard);

    public CompletableFuture<Void> updateBankCard(String number, BankCard bankCard);

    public CompletableFuture<Void> deleteBankCard(String number);

}
