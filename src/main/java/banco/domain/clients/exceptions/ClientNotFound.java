package banco.domain.clients.exceptions;

public class ClientNotFound extends RuntimeException {
    public ClientNotFound(String message) {
        super("Cliente con id " + message + " no encontrado");
    }
}
