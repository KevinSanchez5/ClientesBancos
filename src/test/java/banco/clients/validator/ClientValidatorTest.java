package banco.clients.validator;

import banco.clients.exceptions.ClientException;
import banco.clients.exceptions.ClientExceptionBadRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientValidatorTest {

    ClientValidator validator = new ClientValidator();

    @Test
    void validateString() throws ClientExceptionBadRequest {
        //Arrange
        String cadena = "     cadenaTest   ";
        //Act
        String result = validator.validateString(cadena, "nombre");
        //Assert
        assertAll(
                () -> assertNotNull(result),
                () -> assertNotEquals(cadena, result),
                () -> assertEquals("cadenaTest", result),
                () -> assertDoesNotThrow(() -> validator.validateString("cadenaTest", "nombre"))
        );
    }

    @Test
    void validateStringEmpty() {
        //Arrange
        String cadena = "     ";
        //Act
        var result = assertThrows(ClientExceptionBadRequest.class, () -> validator.validateString(cadena, "nombre"));
        //Assert
        assertEquals("El nombre del cliente es incorrecto", result.getMessage());
        assertThrows(ClientExceptionBadRequest.class, () -> validator.validateString(cadena, "nombre"));
    }

    @Test
    void validateEmail() throws ClientException {
        //Arrange
        String email = "example@example.ex";
        //Act
        String res = validator.validateEmail(email);
        //Assert
        assertAll(
                () -> assertNotNull(res),
                () -> assertEquals(email, res)
        );
    }

    @Test
    void validateEmailEmpty() {
        //Arrange
        String email = "     ";
        //Act
        var result = assertThrows(ClientExceptionBadRequest.class, () -> validator.validateEmail(email));
        //Assert
        assertEquals("El email del cliente no puede estar en blanco", result.getMessage());

    }

    @Test
    void validateEmailInvalid() {
        //Arrange
        String email = "example@.ex";
        //Act
        var result = assertThrows(ClientExceptionBadRequest.class, () -> validator.validateEmail(email));
        //Assert
        assertEquals("El email del cliente no es valido", result.getMessage());

    }
}