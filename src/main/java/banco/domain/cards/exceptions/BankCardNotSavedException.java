package banco.domain.cards.exceptions;

public class BankCardNotSavedException extends BankCardException{
    public BankCardNotSavedException(String message) {
        super(message);
    }
}
