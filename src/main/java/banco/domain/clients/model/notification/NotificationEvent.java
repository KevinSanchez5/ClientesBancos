package banco.domain.clients.model.notification;


import banco.domain.clients.model.Client;
import lombok.Data;

/**
 * Evento de notificación
 */
@Data
public class NotificationEvent {
    private final NotificationType type; //CREATE, UPDATE, DELETE
    private final Client client;

    /**
     * Constructor
     * @param type Tipo de evento
     * @param clientDto Cliente
     */
    public NotificationEvent(NotificationType type, Client clientDto) {
        this.type = type;
        this.client = clientDto;
    }

    /**
     *  Método toString para mostrar el evento
     * @return
     */
    public String toString() {
        return "CAMBIO EN CLIENTE (Tipo= " + this.type + ", client=" + this.client.toString() + ")";
    }
}
