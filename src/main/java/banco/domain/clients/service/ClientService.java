package banco.domain.clients.service;

import banco.domain.cards.exceptions.BankCardNotFoundException;
import banco.domain.cards.model.BankCard;
import banco.domain.clients.exceptions.ClientExceptionBadRequest;
import banco.domain.clients.exceptions.ClientNotFoundException;
import banco.domain.clients.model.Client;

import java.util.List;

public interface ClientService {

    public List<Client> findAllClients();

    public Client findByClientId(Long id);

    public Client saveClient(Client client) throws ClientExceptionBadRequest;

    public Client updateClient(Long id, Client client) throws ClientExceptionBadRequest, ClientNotFoundException;

    public void deleteClient(Long id);

    public List<BankCard> findAllBankCards();

    public List<BankCard> findBankCardsByClientId(Long id);

    public BankCard findBankCardByNumber(String number);

    public BankCard saveBankCard(BankCard bankCard);

    public BankCard updateBankCard(String number, BankCard bankCard) throws BankCardNotFoundException;

    public void deleteBankCard(String number) throws BankCardNotFoundException;


}
