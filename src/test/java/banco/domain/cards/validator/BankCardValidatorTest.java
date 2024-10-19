package banco.domain.cards.validator;

import banco.domain.cards.model.BankCard;
import banco.domain.cards.exceptions.BankCardException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BankCardValidatorTest {
    private BankCardValidator validator;
    private BankCard card;

    @BeforeEach
    public void setUp() {
        validator = new BankCardValidator();
        card = Mockito.mock(BankCard.class);
    }

    @Test
    public void testValidCard() {
        when(card.getNumber()).thenReturn("4800-4586-8771-1006");
        when(card.getExpirationDate()).thenReturn(LocalDate.of(2025, 12, 31));
        assertDoesNotThrow(() -> validator.validate(card));
    }

    @Test
    public void testInvalidCardNumberFormat() {
        when(card.getNumber()).thenReturn("4800-4088-06942362");
        when(card.getExpirationDate()).thenReturn(LocalDate.of(2025, 12, 31));

        BankCardException exception = assertThrows(BankCardException.class, () -> validator.validate(card));

        assertEquals("El número de la tarjeta no tiene el formato correcto. Debe ser XXXX-XXXX-XXXX-XXXX", exception.getMessage());
    }

    @Test
    public void testCardNumberTooShort() {
        when(card.getNumber()).thenReturn("4800-4547-2963-05");
        when(card.getExpirationDate()).thenReturn(LocalDate.of(2025, 12, 31));

        BankCardException exception = assertThrows(BankCardException.class, () -> validator.validate(card));

        assertEquals("El número de la tarjeta no tiene el formato correcto. Debe ser XXXX-XXXX-XXXX-XXXX", exception.getMessage());
    }

    @Test
    public void testCardNumberWithInvalidCharacters() {
        when(card.getNumber()).thenReturn("4800-5486-33A0-5465");
        when(card.getExpirationDate()).thenReturn(LocalDate.of(2025, 12, 31));

        BankCardException exception = assertThrows(BankCardException.class, () -> validator.validate(card));

        assertEquals("El número de la tarjeta caracteres inválidos. Debe ser XXXX-XXXX-XXXX-XXXX", exception.getMessage());
    }

    @Test
    public void testNonExpiredCard() {
        when(card.getNumber()).thenReturn("4800-5486-3320-5465");
        when(card.getExpirationDate()).thenReturn(LocalDate.of(2025, 12, 31));
        assertDoesNotThrow(() -> validator.validate(card));
    }

    @Test
    public void testExpiredCard() {
        when(card.getNumber()).thenReturn("4800-3185-1859-0393");
        when(card.getExpirationDate()).thenReturn(LocalDate.of(2020, 12, 31));

        BankCardException exception = assertThrows(BankCardException.class, () -> validator.validate(card));

        assertEquals("La tarjeta ha expirado. Fecha: 2020-12-31", exception.getMessage());
    }

    @Test
    public void testCardNumberNull() {
        when(card.getNumber()).thenReturn(null);
        when(card.getExpirationDate()).thenReturn(LocalDate.of(2025, 12, 31));

        BankCardException exception = assertThrows(BankCardException.class, () -> validator.validate(card));

        assertEquals("El número de la tarjeta no tiene el formato correcto. Debe ser XXXX-XXXX-XXXX-XXXX", exception.getMessage());
    }

    @Test
    public void testExpirationDateNull() {
        when(card.getNumber()).thenReturn("4800-3185-1859-0393");
        when(card.getExpirationDate()).thenReturn(null);

        BankCardException exception = assertThrows(BankCardException.class, () -> validator.validate(card));

        assertEquals("La fecha de caducidad no puede estar vacía", exception.getMessage());
    }
}