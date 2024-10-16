package banco.domain.clients.notification.models;


import banco.domain.clients.model.Client;
import lombok.Data;

@Data
public class NotificationEvent {
    private final NotificationType type;
    private final Client client;

    public NotificationEvent(NotificationType type, Client clientDto) {
        this.type = type;
        this.client = clientDto;
    }

    public String toString() {
        return "CAMBIO EN CLIENTE (Tipo= " + this.type + ", client=" + this.client.toString() + ")";
    }
}
