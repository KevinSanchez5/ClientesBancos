package banco.domain.clients.service.notification;

import banco.domain.clients.model.Client;
import banco.domain.clients.model.notification.NotificationEvent;
import banco.domain.clients.model.notification.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotificationServiceTest {

    public class DummyClass {
        public List<NotificationEvent> notifications = new ArrayList<>();

        public DummyClass(NotificationService notificationService) {
            notificationService.subscribe(event -> notifications.add(event));
        }
    }

    private NotificationService notificationService;
    Client client = new Client(1L, "John Doe", "12345678A", "example.com");
    DummyClass dummyClass;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService();
        dummyClass = new DummyClass(notificationService);
    }

    @Test
    void testSendNotification() throws InterruptedException {
        NotificationEvent event = new NotificationEvent(NotificationType.CREATE, client);

        // Envía una notificación
        notificationService.sendNotification(event);

        // Verifica que la notificación se recibe correctamente en la lista de DummyClass
        assertEquals(1, dummyClass.notifications.size());
        assertEquals(event, dummyClass.notifications.get(0));
    }
}