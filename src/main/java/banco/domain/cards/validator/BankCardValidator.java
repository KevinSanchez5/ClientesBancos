package banco.domain.cards.validator;

import banco.domain.cards.exceptions.BankCardException;
import banco.domain.cards.model.BankCard;

import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Clase para validar los datos de las tarjetas de crédito.
 */
public class BankCardValidator {
    private static final Pattern BANKCARD_PATTERN = Pattern.compile("^\\d{4}-\\d{4}-\\d{4}-\\d{4}$");

    /**
     * Valida una tarjeta de crédito.
     *
     * @param bankCard La tarjeta de crédito a validar.
     * @throws BankCardException Si la tarjeta de crédito no pasa las validaciones.
     */
    public void validate(BankCard bankCard) throws BankCardException {
        validateCardNumber(bankCard.getNumber());
        validateExpirationDate(bankCard.getExpirationDate());
    }

    /**
     * Valida el número de la tarjeta de crédito.
     *
     * @param number El número de la tarjeta de crédito.
     * @throws BankCardException Si el número de la tarjeta no tiene el formato correcto.
     */
    private void validateCardNumber(String number) throws BankCardException {
        if (number == null || !BANKCARD_PATTERN.matcher(number).matches()) {
            throw new BankCardException("El número de la tarjeta no tiene el formato correcto. Debe ser XXXX-XXXX-XXXX-XXXX");
        }
    }

    /**
     * Valida la fecha de caducidad de la tarjeta de crédito.
     *
     * @param expirationDate La fecha de caducidad de la tarjeta de crédito.
     * @throws BankCardException Si la fecha de caducidad está vacía o la tarjeta ha expirado.
     */
    private void validateExpirationDate(LocalDate expirationDate) throws BankCardException {
        if (expirationDate == null) {
            throw new BankCardException("La fecha de caducidad no puede estar vacía");
        }

        LocalDate currentDate = LocalDate.now();
        if (expirationDate.isBefore(currentDate)) {
            throw new BankCardException("La tarjeta ha expirado. Fecha: " + expirationDate);
        }
    }
}
